package lv.ebit.jira.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.plugin.projectpanel.impl.AbstractProjectTabPanel;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.projects.api.sidebar.ProjectScopeFilterContextProvider;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;


public class SladiatorProjectTab implements ContextProvider {// implements ProjectScopeFilterContextProvider { //extends AbstractProjectTabPanel {
	private final ApplicationProperties applicationProperties;
	private final PluginSettingsFactory pluginSettingsFactory;
	private final PermissionManager permissionManager;
	//private boolean isProjectLead;	
	
	private Map<String, String> params;
	
	public SladiatorProjectTab(ApplicationProperties applicationProperties, PluginSettingsFactory pluginSettingsFactory) {
        this.applicationProperties = applicationProperties;
        this.pluginSettingsFactory = pluginSettingsFactory;
        permissionManager = ComponentAccessor.getPermissionManager();
    }

	//@Override
	public Map<String, Object> getContext(Project project) {
		Map<String, Object> velocityParams = new HashMap<String, Object>();
		ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
		boolean isProjectLead = (project.getLeadUserName() == user.getName() || 
								permissionManager.hasPermission(ProjectPermissions.ADMINISTER_PROJECTS, project, user, false));
		
		velocityParams.put("isProjectLead", isProjectLead);
        velocityParams.put("baseURL", applicationProperties.getBaseUrl());
        velocityParams.put("serviceURL", SladiatorIssueListener.getServiceUrl());
        
        velocityParams.put("projectId", project.getId());
        PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(SladiatorConfigModel.KEY);
        String projectId = project.getId().toString(); 
        Object config = pluginSettings.get(projectId);
		velocityParams.put("sla", new SladiatorConfigModel(config));
		
		List<String> errors = new ArrayList<String>();
		if (pluginSettings.get("errors"+project.getId().toString()) != null) {
        	errors = new ArrayList<String>(Arrays.asList(pluginSettings.get("errors"+project.getId().toString()).toString().split(",")));
        	errors.remove("");
        }
		velocityParams.put("errors",errors);
		velocityParams.put("issue_url",applicationProperties.getBaseUrl()+"/browse/");
		
		List<String> issueTypes = new ArrayList<String>();
		for(Iterator<IssueType> iterator = ComponentAccessor.getIssueTypeSchemeManager().getIssueTypesForProject(project).iterator(); iterator.hasNext();) {
			issueTypes.add(iterator.next().getId());
		}
		
		CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
		List<CustomField> customFields = customFieldManager.getCustomFieldObjects(project.getId(), issueTypes);
		velocityParams.put("customFields",customFields);

		return velocityParams;
	}

	@Override
	public void init(Map<String, String> params) throws PluginParseException {
		this.params = params;
	}

	@Override
	public Map<String, Object> getContextMap(Map<String, Object> context) {
		Project project = (Project)context.get("project");
		return getContext(project);
	}
}
