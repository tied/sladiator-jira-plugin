package lv.ebit.jira.plugins;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
//import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.project.DefaultProjectManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;


@Path("/")
public class SladiatorRestResource {
	public static final Logger log = LoggerFactory.getLogger(SladiatorRestResource.class);
	private final PluginSettingsFactory pluginSettingsFactory;
	private final TransactionTemplate transactionTemplate;
//	private final AvatarService avatarService;
	private final SearchProvider searchProvider;
	private final UserManager userManager;
	private final String jiraUrl;
	
	public SladiatorRestResource(PluginSettingsFactory pluginSettingsFactory, TransactionTemplate transactionTemplate, UserManager userManager, ApplicationProperties applicationProperties, SearchProvider searchProvider) {
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.transactionTemplate = transactionTemplate;
		this.userManager = userManager;
		this.jiraUrl = applicationProperties.getBaseUrl();
//		this.avatarService = avatarService;
		this.searchProvider = searchProvider;
	}
	
	@Path("/config")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putConfig(final SladiatorConfigModel config,@Context HttpServletRequest request) {
		if (!isAuthorized(request, config.getProject())) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		String status = SladiatorTransport.ping(SladiatorIssueListener.getServiceUrl(), config.getSlaToken());
		if (config.isValid() && status.isEmpty()) {
			transactionTemplate.execute(new TransactionCallback() {
				public Object doInTransaction() {				
					PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(SladiatorConfigModel.KEY);
					pluginSettings.put(config.getProject(),config.toString());
					return true;
				}
			});
			return Response.ok("Configuration saved and tested successfully").build();
		} else {

			if (config.isValid()){
				return Response.serverError().entity("Problems with connection to SLAdiator: "+ status).build();
			} else {
				return Response.serverError().entity(config.getErrorMessages()).build();
			}
			
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
				pluginSettings.remove(config.getProject());
				pluginSettings.remove("errors"+config.getProject());
				return true;
			}
		});
		return Response.ok("Configuration deleted successfully").build();		
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
			errors = "Starting from date is required.";
		}
		if (teleport.project.isEmpty()) {
			errors = errors + " Project is required.";
		}
		Date date_from = null;
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		try {
			date_from = formatter.parse(teleport.date_from);
		} catch (ParseException e) {
			errors = errors+ " Invalid date format.";
		}
		SladiatorConfigModel config = getSladiatorConfig(teleport.project);
		String status = SladiatorTransport.ping(SladiatorIssueListener.getServiceUrl(), config.getSlaToken());
		
		if (!status.isEmpty()) {
			errors = errors + " "+status;
		}
		if (errors.isEmpty()) {
			SladiatorTeleport job = new SladiatorTeleport(config, this.jiraUrl, date_from, this.searchProvider, new DefaultProjectManager().getProjectObj(Long.valueOf(teleport.project)).getLead());
			job.run();
			String success = job.getTotalProcessed() + " issues sent to RealSLA";
			return Response.ok(success).build();
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
		
		SladiatorConfigModel config = getSladiatorConfig(janitor.project);
		String status = SladiatorTransport.ping(SladiatorIssueListener.getServiceUrl(), config.getSlaToken());
		
		if (status.isEmpty()) {
			
			ArrayList<String> keys = SladiatorIssueListener.getFailedIssues(janitor.project);
			SladiatorJanitor job = new SladiatorJanitor(config, this.jiraUrl, keys, this.searchProvider, new DefaultProjectManager().getProjectObj(Long.valueOf(janitor.project)).getLead());
			job.run();
			return Response.ok().build();
		} else {
			return Response.serverError().entity(status).build();
			
		}
		
	}
	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static final class JanitorModel {
		@XmlElement
		private String project;
	}
	
	@Path("/admin")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response adminConfig(final AdminConfigModel config,@Context HttpServletRequest request) {
		if (!isAdmin(request)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		if (config.service_url.isEmpty()) {
			return Response.serverError().entity("URL is not present").build();
		} else {
			SladiatorIssueListener.setServiceUrl(config.service_url);
			return Response.ok("Configuration saved successfully").build();
		}
		
				
	}
	
	@Path("/test_service")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response testService(final AdminConfigModel config,@Context HttpServletRequest request) {
		if (!isAdmin(request)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		if (config.service_url.isEmpty()) {
			return Response.serverError().entity("URL is not present").build();
		} else {
			String status = SladiatorTransport.ping(config.service_url,"");
			if (status.isEmpty()) {
				return Response.ok("Service URL is valid").build();
			} else {
				return Response.serverError().entity(status).build();
			}
			
		}
		
				
	}
	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static final class AdminConfigModel {
		@XmlElement
		private String service_url;
	}
	private boolean isAuthorized(HttpServletRequest request, String projectId) {
		Project project = new DefaultProjectManager().getProjectObj(Long.valueOf(projectId));
		PermissionManager permissionManager = ComponentManager.getInstance().getPermissionManager();
		com.opensymphony.user.User user = (com.opensymphony.user.User)userManager.resolve(userManager.getRemoteUsername(request));
	
		if (project.getLeadUserName() == user.getName() || permissionManager.hasPermission(23, project, user)) {
			return true;
		}
		return false;
	}
	
	private boolean isAdmin(HttpServletRequest request) {
		String username = userManager.getRemoteUsername(request);
        if (username == null || username != null && !userManager.isSystemAdmin(username))
        {
            return false;
        }
        return true;
	}
	private SladiatorConfigModel getSladiatorConfig(String project) {
		PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(SladiatorConfigModel.KEY);
		return new SladiatorConfigModel(pluginSettings.get(project));
	}
}
