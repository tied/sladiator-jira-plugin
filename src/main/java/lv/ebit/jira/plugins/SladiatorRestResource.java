package lv.ebit.jira.plugins;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.jira.project.DefaultProjectManager;
import com.atlassian.jira.project.Project;


@Path("/")
public class SladiatorRestResource {
	public static final Logger log = LoggerFactory.getLogger(SladiatorRestResource.class);
	private final PluginSettingsFactory pluginSettingsFactory;
	private final TransactionTemplate transactionTemplate;
	private final UserManager userManager;
	
	public SladiatorRestResource(PluginSettingsFactory pluginSettingsFactory, TransactionTemplate transactionTemplate, UserManager userManager) {
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.transactionTemplate = transactionTemplate;
		this.userManager = userManager;
	}
	
	@Path("/config")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putConfig(final SladiatorConfigModel config,@Context HttpServletRequest request) {
		if (!isAuthorized(request, config.getProject())) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		transactionTemplate.execute(new TransactionCallback() {
			public Object doInTransaction() {
				if (config.isValid()) {
					PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(SladiatorConfigModel.KEY);
					pluginSettings.put(config.getProject().toString(),config.toString());
					return true;
				} else {
					return false;
				}
			}
		});
		if (config.isValid()) {
			return Response.ok("Configuration saved successfully").build();
		} else {
			return Response.serverError().entity(config.getErrorMessages()).build();
		}		
	}
	
	@Path("/config")
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removeConfig(final SladiatorConfigModel config,@Context HttpServletRequest request) {
		if (!isAuthorized(request, config.getProject())) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		transactionTemplate.execute(new TransactionCallback() {
			public Object doInTransaction() {
				PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(SladiatorConfigModel.KEY);
				pluginSettings.remove(config.getProject().toString());
				return true;
			}
		});
		return Response.ok("Configuration deleted successfully").build();		
	}
	
	@Path("/connection")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response connection(SladiatorConfigModel config, @Context HttpServletRequest request) {
		if (!isAuthorized(request, config.getProject())) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		if (config.isValid()) {
			SladiatorTransport job = new SladiatorTransport(config);
			String status = job.checkConnection();
			if (status.isEmpty()) {
				return Response.ok("Connection to SLAdiator was succesfull").build();
			} else {
				return Response.serverError().entity("Problems with connection to SLAdiator: "+ status).build();
			}
			
		} else {
			return Response.serverError().entity("Not all configuratio options are set!").build();
		}
	}
	
	@Path("/teleport")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response teleport(final TeleportModel teleport, @Context HttpServletRequest request) {
		if (!isAuthorized(request, teleport.project)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		String errors = "";
		if (teleport.date_from.isEmpty()) {
			errors = "Starting from date is required";
		}
		if (teleport.project.isEmpty()) {
			errors = "Project is required";
		}
		if (errors.isEmpty()) {
			Date date_from = null;
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			try {
				date_from = formatter.parse(teleport.date_from);
			} catch (ParseException e) {
				errors = "Invalid date format.";
			}
			return Response.ok("XXX tickets sent to SLAdiator").build();
		} else {
			return Response.serverError().entity(errors).build();
		}
	}
	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static final class TeleportModel {
		@XmlElement
		private String date_from;
		@XmlElement
		private String project;
	}
	
	@Path("/janitor")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response janitor(final JanitorModel janitor, @Context HttpServletRequest request) {
		if (!isAuthorized(request, janitor.project)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		return Response.ok().build();
		
	}
	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static final class JanitorModel {
		@XmlElement
		private String project;
	}
	
	private boolean isAuthorized(HttpServletRequest request, String projectId) {
		Project project = new DefaultProjectManager().getProjectObj(Long.valueOf(projectId));
		String username = userManager.getRemoteUsername(request);
		if (username == null || username != null && project.getLeadUser().getName() != username) {
			return false;
		}
		return true;
	}
}
