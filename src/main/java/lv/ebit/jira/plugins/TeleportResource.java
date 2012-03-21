package lv.ebit.jira.plugins;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lv.ebit.jira.plugins.ConfigModel.Configuration;

import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.crowd.embedded.api.User;
import com.google.common.base.Joiner;
import com.opensymphony.user.EntityNotFoundException;

@Path("/teleport/")
public class TeleportResource {
	private final UserManager userManager;
	private final PluginSettingsFactory pluginSettingsFactory;
	private final SearchProvider searchProvider;
	private final AvatarService avatarService;
	private final ApplicationProperties applicationProperties;

	public TeleportResource(UserManager userManager, PluginSettingsFactory pluginSettingsFactory, SearchProvider searchProvider, AvatarService avatarService, ApplicationProperties applicationProperties) {
		this.userManager = userManager;
		this.searchProvider = searchProvider;
		this.avatarService = avatarService;
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.applicationProperties = applicationProperties;
		
	}

	@SuppressWarnings("deprecation")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post(final TeleportModel config, @Context HttpServletRequest request) {

		if (!isAuthorized(request)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		String errors = "";
		String success = "";
		Date date_from = null;
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		try {
			date_from = formatter.parse(config.date_from);
		} catch (ParseException e) {
			errors = "Invalid date format.";
		}
		try {
			config.sla.get(0).toString();
		} catch (NullPointerException e) {
			errors = errors + "Select at least one SLA.";
		}
		if (errors.isEmpty()) {
			User user = null;
			//starting from 4.4			
			// user = com.atlassian.jira.user.UserUtils.getUser(request.getRemoteUser());
			try {
				user = com.opensymphony.user.UserManager.getInstance().getUser(request.getRemoteUser());
			} catch (EntityNotFoundException e1) {
				
			}
			
			TeleportJob job = new TeleportJob(date_from, config.sla, pluginSettingsFactory, searchProvider, avatarService, applicationProperties, user);
			job.run();
			success = job.getTotalProcessed() + " issues sent to RealSLA";
			return Response.ok(success).build();
		} else {
			return Response.serverError().entity(errors).build();
		}

	}
	
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delete(@Context HttpServletRequest request) {
		PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(Configuration.KEY);
		pluginSettings.remove("errors");
		return Response.ok().build();
	}
	
	private boolean isAuthorized(HttpServletRequest request) {
		String username = userManager.getRemoteUsername(request);
		if (username == null || username != null && !userManager.isSystemAdmin(username)) {
			return false;
		}
		return true;
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static final class TeleportModel {
		@XmlElement
		private String date_from;
		@XmlElement
		private List<String> sla;
	}
}
