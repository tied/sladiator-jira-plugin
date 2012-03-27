package lv.ebit.jira.plugins;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.plugin.projectpanel.impl.AbstractProjectTabPanel;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.sal.api.ApplicationProperties;


public class SladiatorProjectTab extends AbstractProjectTabPanel {
	private final ApplicationProperties applicationProperties;
	
	public SladiatorProjectTab(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }
	
	public String getHtml(BrowseContext ctx) {
		Map<String, Object> velocityParams = new HashMap<String, Object>();
		Project project = ctx.getProject();
        User user = ctx.getUser();
        PermissionManager tmpPermissionMgmt = ComponentManager.getInstance().getPermissionManager();
        boolean isProjectAdmin = tmpPermissionMgmt.hasPermission(23, project, user);
        velocityParams.put("isProjectAdmin", isProjectAdmin);
        velocityParams.put("baseURL", applicationProperties.getBaseUrl());
		return descriptor.getHtml("config", velocityParams);
		
	}
	@Override
	public boolean showPanel(BrowseContext browseContext) {
		return true;
	}

}
