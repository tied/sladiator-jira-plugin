package lv.ebit.jira.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.plugin.projectpanel.impl.AbstractProjectTabPanel;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;


public class SladiatorProjectTab extends AbstractProjectTabPanel {
	private final ApplicationProperties applicationProperties;
	private final PluginSettingsFactory pluginSettingsFactory;
	
	public SladiatorProjectTab(ApplicationProperties applicationProperties, PluginSettingsFactory pluginSettingsFactory) {
        this.applicationProperties = applicationProperties;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }
	
	@SuppressWarnings("deprecation")
	public String getHtml(BrowseContext ctx) {
		Map<String, Object> velocityParams = new HashMap<String, Object>();
		Project project = ctx.getProject();
		
		velocityParams.put("isProjectLead", (project.getLeadUser().getName() == ctx.getUser().getName()));
        velocityParams.put("baseURL", applicationProperties.getBaseUrl());
        
        velocityParams.put("projectId", project.getId());
        PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(SladiatorConfigModel.KEY);
		velocityParams.put("sla", new SladiatorConfigModel(pluginSettings.get(ctx.getProject().getId().toString())));
		
		List<String> errors = new ArrayList<String>();
		if (pluginSettings.get("errors"+project.getId().toString()) != null) {
        	errors = new ArrayList<String>(Arrays.asList(pluginSettings.get("errors"+project.getId().toString()).toString().split(",")));
        	errors.remove("");
        }
		velocityParams.put("errors",errors);
		velocityParams.put("issue_url",applicationProperties.getBaseUrl()+"/browse/");
		return descriptor.getHtml("config", velocityParams);
		
	}
	@Override
	public boolean showPanel(BrowseContext browseContext) {
		return true;
	}

}
