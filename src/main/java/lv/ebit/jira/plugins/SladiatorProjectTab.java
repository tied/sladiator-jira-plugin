package lv.ebit.jira.plugins;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.plugin.projectpanel.impl.AbstractProjectTabPanel;
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
        velocityParams.put("isProjectLead", (ctx.getProject().getLeadUser().getName() == ctx.getUser().getName()));
        velocityParams.put("baseURL", applicationProperties.getBaseUrl());
        velocityParams.put("projectId", ctx.getProject().getId());
        PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(SladiatorConfigModel.KEY);
		velocityParams.put("sla", new SladiatorConfigModel(pluginSettings.get(ctx.getProject().getId().toString())));
		
		return descriptor.getHtml("config", velocityParams);
		
	}
	@Override
	public boolean showPanel(BrowseContext browseContext) {
		return true;
	}

}
