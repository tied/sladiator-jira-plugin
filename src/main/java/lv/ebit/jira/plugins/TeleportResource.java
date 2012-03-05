package lv.ebit.jira.plugins;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.sal.api.user.UserManager;



@Path("/teleport/")
public class TeleportResource {
	private final UserManager userManager;
	private final SearchProvider searchProvider;
	private final AvatarService avatarService;
	
	public TeleportResource(UserManager userManager,SearchProvider searchProvider, AvatarService avatarService) {
		this.userManager = userManager;
		this.searchProvider = searchProvider;
		this.avatarService = avatarService;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post(final TeleportModel config, @Context HttpServletRequest request) {
		
		if (!isAuthorized(request)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		String errors = "";
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		try {
			TeleportJob job = new TeleportJob(formatter.parse(config.date_from), searchProvider, avatarService, UserUtils.getUser(request.getRemoteUser()));
			job.run();
			errors = "Processed "+job.getTotalProcessed()+" issues";
		} catch (ParseException e) {
			errors = "Invalid date format";
			return Response.serverError().entity(errors).build();
		}
		return Response.ok(errors).build();
		
	}
	private boolean isAuthorized(HttpServletRequest request) {
		String username = userManager.getRemoteUsername(request);
		if (username == null || username != null && !userManager.isSystemAdmin(username)) {
			return false;
		}
		return true;
	}
	
	@XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
	public static final class TeleportModel {
		@XmlElement private String date_from;
		
	}
}
