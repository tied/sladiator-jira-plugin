package lv.ebit.jira.plugins;

import java.util.ArrayList;
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

public class SladiatorJanitor {
	private final SearchProvider searchProvider;
	private final AvatarService avatarService;
	private SladiatorConfigModel config;
	private String jiraUrl;
	private ApplicationUser user;
	private ArrayList<String> keys;
	
	public SladiatorJanitor(SladiatorConfigModel config, String jiraUrl, ArrayList<String> keys, SearchProvider searchProvider, AvatarService avatarService, ApplicationUser user){
		this.config = config;
		this.jiraUrl = jiraUrl;
		this.searchProvider = searchProvider;
		this.avatarService = avatarService;
		this.user = user;
		this.keys = keys;
	}
	public void run() {
		JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
		Query query = queryBuilder.where().project().eq(this.config.getProject()).and().issue().inStrings(this.keys).buildQuery();
//		SladiatorTransport.log.error("query "+query.getWhereClause());
		try {
//			SladiatorTransport.log.error("query returned "+this.searchProvider.searchCount(query, user));
			PagerFilter<Object> pager = new PagerFilter<Object>(10000);
			SearchResults results = searchProvider.search(query, user, pager);
			List<Issue> issues = results.getIssues();
			for (Issue issue : issues) {
				Runnable transport = new SladiatorTransport(this.config, this.jiraUrl,EventType.ISSUE_CREATED_ID, issue, this.avatarService);
				transport.run();
			}
		} catch (SearchException e) {
			SladiatorTransport.log.error("SearchException :"+e.getMessage());
		}
	}
}
