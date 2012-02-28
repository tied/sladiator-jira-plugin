package lv.ebit.jira.plugins;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;

import com.atlassian.templaterenderer.TemplateRenderer;

import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.Project;

import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


public class ConfigServlet extends HttpServlet {
	
	private final UserManager userManager;
	private final LoginUriProvider loginUriProvider;
	private final TemplateRenderer renderer;
	private final ProjectManager projectManager;
	
	public ConfigServlet(UserManager userManager, LoginUriProvider loginUriProvider, TemplateRenderer renderer, ProjectManager projectManager) {
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.renderer = renderer;
        this.projectManager = projectManager;
    }
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
		String username = userManager.getRemoteUsername(request);
        if (username == null || username != null && !userManager.isSystemAdmin(username))
        {
            redirectToLogin(request, response);
            return;
        }
        
        Map<String, Object> velocityParams = new HashMap<String, Object>();
        List<Project> projects = this.projectManager.getProjectObjects();
        velocityParams.put("projects", projects);
        response.setContentType("text/html;charset=utf-8");
        renderer.render("templates/config.vm",velocityParams, response.getWriter());
    }
	
	private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    }

    private URI getUri(HttpServletRequest request)
    {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null)
        {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }
}
