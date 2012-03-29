package lv.ebit.jira.plugins;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;

public class SladiatorAdminServlet extends HttpServlet {
	private static final long serialVersionUID = -4281704355836541119L;
	private final TemplateRenderer renderer;
	private final LoginUriProvider loginUriProvider;
	private final UserManager userManager;
	private final ApplicationProperties applicationProperties;
	
	public SladiatorAdminServlet(TemplateRenderer renderer, LoginUriProvider loginUriProvider, UserManager userManager, ApplicationProperties applicationProperties){
		this.renderer = renderer;
		this.loginUriProvider = loginUriProvider;
		this.userManager = userManager;
		this.applicationProperties = applicationProperties;
	}
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String username = userManager.getRemoteUsername(request);
        if (username == null || username != null && !userManager.isSystemAdmin(username))
        {
            redirectToLogin(request, response);
            return;
        }
        
		Map<String, Object> velocityParams = new HashMap<String, Object>();
		
		velocityParams.put("service_url",SladiatorIssueListener.getServiceUrl());
		velocityParams.put("baseURL", applicationProperties.getBaseUrl());
		response.setContentType("text/html;charset=utf-8");
        renderer.render("templates/admin.vm",velocityParams, response.getWriter());
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
