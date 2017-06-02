package lv.ebit.jira.plugins;

import java.util.Date;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;

public class SladiatorTeleport {
	private final SearchProvider searchProvider;
	private final AvatarService avatarService;
	private SladiatorConfigModel config;
	private String jiraUrl;
	private Long total;
	private ApplicationUser user;
	private Date dateFrom;
	
	public SladiatorTeleport(SladiatorConfigModel config, String jiraUrl, Date dateFrom, SearchProvider searchProvider, AvatarService avatarService, ApplicationUser user) {
		this.config = config;
		this.dateFrom = dateFrom;
		this.jiraUrl = jiraUrl;
		this.searchProvider = searchProvider;
		this.avatarService = avatarService;
		this.user = user;
	}
	
	public void run() {
		try {
			JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
			Query query = queryBuilder.where().createdAfter(dateFrom).and().project().eq(this.config.getProject()).buildQuery();
			this.total = searchProvider.searchCount(query, user);
			PagerFilter<Object> pager = new PagerFilter<Object>(10000);
			SearchResults results = searchProvider.search(query, user, pager);
			List<Issue> issues = results.getIssues();
			for (Issue issue : issues) {
				Runnable transport = new SladiatorTransport(this.config, this.jiraUrl,EventType.ISSUE_CREATED_ID, issue, this.avatarService);
				transport.run();
			}
		} catch (SearchException e) {
			e.printStackTrace();
		}
	}
	
	public Long getTotalProcessed() {
		return this.total;
	}
}
