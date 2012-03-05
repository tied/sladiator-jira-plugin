package lv.ebit.jira.plugins;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;

import lv.ebit.jira.plugins.ConfigModel;
import lv.ebit.jira.plugins.ConfigModel.Configuration;

@Path("/")
public class ConfigResource {
	private final UserManager userManager;
	private final PluginSettingsFactory pluginSettingsFactory;
	private final TransactionTemplate transactionTemplate;
//	private static final Logger log = LoggerFactory.getLogger(Configuration.class);

	public ConfigResource(UserManager userManager,
			PluginSettingsFactory pluginSettingsFactory,
			TransactionTemplate transactionTemplate) {
		this.userManager = userManager;
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.transactionTemplate = transactionTemplate;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post(final ConfigModel config, @Context HttpServletRequest request) {
		
		if (!isAuthorized(request)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		transactionTemplate.execute(new TransactionCallback() {
			public Object doInTransaction() {
				PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(Configuration.KEY);
				Configuration configuration = new Configuration(pluginSettings.get("configuration"));
				configuration.add(config);
				if (config.getErrors().isEmpty()) {
					pluginSettings.put("configuration",configuration.toString());
				}
				return null;
			}
		});
		return Response.ok(config.getErrorMessages()).build();
		
	}
	
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delete(final ConfigModel config, @Context HttpServletRequest request) {
		
		if (!isAuthorized(request)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		transactionTemplate.execute(new TransactionCallback() {
			public Object doInTransaction() {
				PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(Configuration.KEY);
				Configuration configuration = new Configuration(pluginSettings.get("configuration"));
				configuration.delete(config);
				pluginSettings.put("configuration",configuration.toString());
				return null;
			}
		});

		return Response.noContent().build();
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response put(final ConfigModel config, @Context HttpServletRequest request) {
		
		if (!isAuthorized(request)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		transactionTemplate.execute(new TransactionCallback() {
			public Object doInTransaction() {
				PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(Configuration.KEY);
				Configuration configuration = new Configuration(pluginSettings.get("configuration"));
				configuration.update(config);
				if (config.getErrors().isEmpty()) {
					pluginSettings.put("configuration",configuration.toString());
				}
				return null;
			}
		});

		return Response.ok(config.getErrorMessages()).build();
	}
	
	private boolean isAuthorized(HttpServletRequest request) {
		String username = userManager.getRemoteUsername(request);
		if (username == null || username != null && !userManager.isSystemAdmin(username)) {
			return false;
		}
		return true;
	}
}