package lv.ebit.jira.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.projectpanel.impl.AbstractProjectTabPanel;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;


public class SladiatorProjectTab extends AbstractProjectTabPanel {
	private final ApplicationProperties applicationProperties;
	private final PluginSettingsFactory pluginSettingsFactory;
	private boolean isProjectLead;
	private Project project;
	
	public SladiatorProjectTab(ApplicationProperties applicationProperties, PluginSettingsFactory pluginSettingsFactory) {
        this.applicationProperties = applicationProperties;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }
	
	public String getHtml(BrowseContext ctx) {
		
		Map<String, Object> velocityParams = new HashMap<String, Object>();
		velocityParams.put("isProjectLead", this.isProjectLead);
        velocityParams.put("baseURL", applicationProperties.getBaseUrl());
        velocityParams.put("serviceURL", SladiatorIssueListener.getServiceUrl());
        
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
		
		List<String> issueTypes = new ArrayList<String>();
		for(Iterator<IssueType> iterator = ComponentManager.getInstance().getIssueTypeSchemeManager().getIssueTypesForProject(this.project).iterator(); iterator.hasNext();) {
			issueTypes.add(iterator.next().getId());
		}
		
		CustomFieldManager customFieldManager = ComponentManager.getInstance().getCustomFieldManager();
		List<CustomField> customFields = customFieldManager.getCustomFieldObjects(this.project.getId(), issueTypes);
		velocityParams.put("customFields",customFields);

		return descriptor.getHtml("config", velocityParams);
		
	}
	@Override
	public boolean showPanel(BrowseContext browseContext) {
		this.project = browseContext.getProject();
		com.atlassian.crowd.embedded.api.User user = browseContext.getUser();
		PermissionManager permissionManager = ComponentManager.getInstance().getPermissionManager();
        
		this.isProjectLead = (project.getLeadUser().getName() == user.getName() || permissionManager.hasPermission(23, this.project, user));
		return this.isProjectLead;
	}

}
