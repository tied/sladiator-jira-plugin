package lv.ebit.jira.plugins;

import java.util.List;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.query.Query;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.crowd.embedded.api.User;

public class TeleportJob {
	public static final Logger log = LoggerFactory.getLogger(TeleportJob.class);
	private SearchProvider searchProvider;
	private AvatarService avatarService;
	private User user;
	private Date dateFrom;
	private Long total;
	
	public TeleportJob(Date dateFrom,SearchProvider searchProvider, AvatarService avatarService, User user) {
		this.avatarService = avatarService;
		this.searchProvider = searchProvider;
		this.user = user;
		this.dateFrom = dateFrom;
	}

	public void run() {
		try {
			
			JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
			Query query = queryBuilder.where().createdAfter(dateFrom).buildQuery();
			log.error("query "+query.getWhereClause());
			this.total = searchProvider.searchCount(query, user);
			PagerFilter<Object> pager = new PagerFilter<Object>(10000);
			SearchResults results = searchProvider.search(query, user, pager);
			List<Issue> issues = results.getIssues();
			for (Issue issue : issues) {
				log.error("Found {} issue", issue.getKey());
			}
		} catch (SearchException e) {
			e.printStackTrace();
		}
	}
	public Long getTotalProcessed() {
		return this.total;
	}
}
