package lv.ebit.jira.plugins;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.jira.util.json.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Map.Entry;

import lv.ebit.jira.plugins.ConfigResource.Config;

@Path("/")
public class ConfigResource {
	private final UserManager userManager;
	private final PluginSettingsFactory pluginSettingsFactory;
	private final TransactionTemplate transactionTemplate;
	private static final Logger log = LoggerFactory.getLogger(ConfigResource.class);

	public ConfigResource(UserManager userManager,
			PluginSettingsFactory pluginSettingsFactory,
			TransactionTemplate transactionTemplate) {
		this.userManager = userManager;
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.transactionTemplate = transactionTemplate;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@Context HttpServletRequest request) {
		if (!isAuthorized(request)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		return Response.ok(
				transactionTemplate.execute(new TransactionCallback() {
					public Object doInTransaction() {
						PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
						return pluginSettings.get(Configuration.KEY).toString();
					}
				})).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post(final Config config, @Context HttpServletRequest request) {
		
		if (!isAuthorized(request)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		transactionTemplate.execute(new TransactionCallback() {
			public Object doInTransaction() {
				PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
				Configuration configuration = new Configuration(pluginSettings.get(Configuration.KEY));
				configuration.add(config);
				pluginSettings.put(Configuration.KEY,configuration.toString());
				return null;
			}
		});

		return Response.noContent().build();
	}
	
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delete(final Config config, @Context HttpServletRequest request) {
		
		if (!isAuthorized(request)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		transactionTemplate.execute(new TransactionCallback() {
			public Object doInTransaction() {
				PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
				Configuration configuration = new Configuration(pluginSettings.get(Configuration.KEY));
				configuration.delete(config);
				pluginSettings.put(Configuration.KEY,configuration.toString());
				return null;
			}
		});

		return Response.noContent().build();
	}
	
//	@PUT
//	@Consumes(MediaType.APPLICATION_JSON)
//	public Response put(final Config config,
//			@Context HttpServletRequest request) {
//		String username = userManager.getRemoteUsername(request);
//		if (username == null || username != null
//				&& !userManager.isSystemAdmin(username)) {
//			return Response.status(Status.UNAUTHORIZED).build();
//		}
//
//		transactionTemplate.execute(new TransactionCallback() {
//			public Object doInTransaction() {
//				PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
//				pluginSettings.put(Configs.class.getName() + ".configs","Configs.storeConfig()");
//				return null;
//			}
//		});
//
//		return Response.noContent().build();
//	}

	@XmlRootElement
	// @XmlAccessorType(XmlAccessType.FIELD)
	public static final class Config {
		@XmlElement
		private String sla;
		public String getSla(){return this.sla;}
		public void setSla(String sla){this.sla = sla;}
		@XmlElement
		private String sla_token;
		public String getSlaToken(){return this.sla_token;}
		public void setSlaToken(String sla_token){this.sla_token = sla_token;}
		@XmlElement
		private List<Integer> projects;
		public List<Integer> getProjects(){return this.projects;}
		public void setProjects(List<Integer> projects){this.projects = projects;}

		public String toString() {
			JSONObject json = new JSONObject(this);
			try {
				json.put("sla", this.getSla());
				json.put("sla_token", this.getSlaToken());
				json.put("projects", this.projects);
			} catch (JSONException e) {}
			return json.toString();
		}
	}

	@XmlRootElement
	// @XmlAccessorType(XmlAccessType.FIELD)
	public static final class Configuration {
		@XmlElement
		private HashMap<String, Config> configs;
		public static final String KEY = "lv.ebit.jira.plugins.realsla";
		
		public void add(Config config) {
			this.configs.put(config.sla_token, config);
		}
		public void delete(Config config) {
			this.configs.remove(config.sla_token);
		}
		public void update(Config config) {
			delete(config);
			add(config);
		}
		public HashMap<String, Config> getConfig() {
			return this.configs;
		}
		public Configuration(Object configs) {
			if (configs == null || configs == "{}") {
				log.error("new config");
				this.configs = new HashMap<String, Config>();
			} else {
				log.error("existing config="+configs);
				this.configs = new HashMap<String, Config>();
				try {
					JSONObject json = new JSONObject(configs.toString());
					log.error("json config="+json.toString());
					Iterator<String> keys = json.keys();
					while (keys.hasNext()) {
						String id = keys.next().toString();
						JSONObject row = json.getJSONObject(id);
						Config config = new Config();
						config.setSla(row.get("sla").toString());
						config.setSlaToken(row.get("sla_token").toString());
						add(config);
						log.error("config row ="+config.toString());
					}
				} 
				catch (JSONException e){}
				
			}
		}
		public String toString() {
			JSONObject json = new JSONObject();
			Iterator<Entry<String, Config>> config = this.configs.entrySet().iterator();
			try {
				while (config.hasNext()) {
					Map.Entry<String, Config> pairs = (Map.Entry<String, Config>)config.next();
					json.put(pairs.getKey(), new JSONObject(pairs.getValue().toString()));
			    }
			} catch (JSONException e) {}
			log.error("json config="+json.toString());
			return json.toString();
		}
	}
	private boolean isAuthorized(HttpServletRequest request) {
		String username = userManager.getRemoteUsername(request);
		if (username == null || username != null && !userManager.isSystemAdmin(username)) {
			return false;
		}
		return true;
	}
}