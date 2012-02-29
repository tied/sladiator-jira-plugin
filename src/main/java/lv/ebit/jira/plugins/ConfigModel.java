package lv.ebit.jira.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class ConfigModel {
	private static final Logger log = LoggerFactory.getLogger(ConfigModel.class);
	private ArrayList<String> errors = new ArrayList<String>();

	@XmlElement
	private String sla;

	public String getSla() {
		return this.sla;
	}

	public void setSla(String sla) {
		this.sla = sla;
	}

	@XmlElement
	private String sla_token;

	public String getSlaToken() {
		return this.sla_token;
	}

	public void setSlaToken(String sla_token) {
		this.sla_token = sla_token;
	}

	@XmlElement
	private List<Long> projects;

	public List<Long> getProjects() {
		return this.projects;
	}

	public void setProjects(List<Long> projects) {
		this.projects = projects;
	}

	public boolean hasProject(Long project) {
		return this.getProjects().contains(project);
	}

	public String selectedProject(Long project) {
		if (this.hasProject(project)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}

	public boolean isValid() {
		if (this.getSla().isEmpty()) {
			this.errors.add("SLA name is required");
		}
		if (this.getSlaToken().isEmpty()) {
			this.errors.add("SLA token is required");
		}
		try {
			this.getProjects().get(0).toString();
		} catch (NullPointerException e) {
			this.errors.add("At least one project is required");
		}
		return this.errors.isEmpty();
	}

	public ArrayList<String> getErrors() {
		return this.errors;
	}

	public String getErrorMessages() {
		return StringUtils.join(this.getErrors(), ".");
	}

	public String projectNamesFromProjects(List<Project> projects) {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < projects.size(); i++) {
			if (this.hasProject(projects.get(i).getId())) {
				list.add(projects.get(i).getName() + " (" + projects.get(i).getKey() + ")");
			}
		}
		return StringUtils.join(list, ", ");
	}

	public String toString() {
		JSONObject json = new JSONObject(this);
		try {
			json.put("sla", this.getSla());
			json.put("sla_token", this.getSlaToken());
			json.put("projects", this.getProjects());
		} catch (JSONException e) {
			log.error("Error:" + e.getMessage());
		}
		return json.toString();
	}

	public static final class Configuration {
		private static final Logger log = LoggerFactory.getLogger(Configuration.class);
		private HashMap<String, ConfigModel> configs;
		public static final String KEY = "lv.ebit.jira.plugins.realsla";

		public void add(ConfigModel config) {
			if (config.isValid()) {
				this.configs.put(config.sla_token, config);
			}
		}

		public void delete(ConfigModel config) {
			this.configs.remove(config.sla_token);
		}

		public void update(ConfigModel config) {
			if (config.isValid()) {
				delete(config);
				add(config);
			}
		}

		public HashMap<String, ConfigModel> getConfig() {
			return this.configs;
		}

		public Configuration(Object configs) {
			// log.error("initializ config " + configs);
			if (configs == null || configs == "{}") {
				this.configs = new HashMap<String, ConfigModel>();
			} else {
				this.configs = new HashMap<String, ConfigModel>();
				try {
					JSONObject json = new JSONObject(configs.toString());
					Iterator<String> keys = json.keys();
					while (keys.hasNext()) {
						String id = keys.next().toString();
						JSONObject row = json.getJSONObject(id);
						ConfigModel config = new ConfigModel();
						config.setSla(row.get("sla").toString());
						config.setSlaToken(row.get("sla_token").toString());
						JSONArray array = (JSONArray) row.get("projects");
						List<Long> list = new ArrayList<Long>();
						for (int i = 0; i < array.length(); i++) {
							list.add(array.getLong(i));
						}
						config.setProjects(list);
						add(config);
					}
				} catch (JSONException e) {
					log.error("Error:" + e.getMessage());
				}

			}
		}

		public List<String> slaTokensForProject(Long project) {
			List<String> list = new ArrayList<String>();
			Iterator<Entry<String, ConfigModel>> configs = this.configs.entrySet().iterator();
			while (configs.hasNext()) {
				ConfigModel config = configs.next().getValue();
				if (config.hasProject(project)) {
					list.add(config.getSlaToken());
				}
			}
			return list;
		}

		public String toString() {
			JSONObject json = new JSONObject();
			Iterator<Entry<String, ConfigModel>> config = this.configs.entrySet().iterator();
			try {
				while (config.hasNext()) {
					Map.Entry<String, ConfigModel> pairs = (Map.Entry<String, ConfigModel>) config.next();
					json.put(pairs.getKey(), new JSONObject(pairs.getValue().toString()));
				}
			} catch (JSONException e) {
				log.error("Error:" + e.getMessage());
			}
			// log.error("new config" + json.toString());
			return json.toString();
		}
	}
}
