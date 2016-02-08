package lv.ebit.jira.plugins;

import java.util.Map;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

public class SladiatorProjectTabViewCondition implements Condition {

	
    private Map<String,String> params;

	
	/**
     * Called after creation and autowiring.
     *
     * @param params The optional map of parameters specified in XML.
     */
    @Override
    public void init(Map<String,String> params) throws PluginParseException {
        this.params = params;
    }
    
    
	/**
     * Determine whether the web fragment should be displayed
     *
     * @return true if the user should see the fragment, false otherwise
     */
	@Override
	public boolean shouldDisplay(Map<String, Object> context) {
		Project project = (Project)context.get("project");
		if(project == null) {
			return false;
		}
		
		ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
        
		return (project.getLeadUserName() == user.getName() || permissionManager.hasPermission(ProjectPermissions.ADMINISTER_PROJECTS, project, user, false));

	}
}
