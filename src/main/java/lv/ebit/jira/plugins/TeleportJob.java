package lv.ebit.jira.plugins;

import java.util.Collection;
import java.util.List;
import java.util.Date;

import lv.ebit.jira.plugins.ConfigModel.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.query.Query;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.crowd.embedded.api.User;

public class TeleportJob {
	public static final Logger log = LoggerFactory.getLogger(TeleportJob.class);
	private final SearchProvider searchProvider;
	private final AvatarService avatarService;
	private final ApplicationProperties applicationProperties;
	private User user;
	private Date dateFrom;
	private Long total;
	private Collection<Long> projects;
	private Configuration configuration;
	
	public TeleportJob(Date dateFrom, List<String> slas, PluginSettingsFactory pluginSettingsFactory, SearchProvider searchProvider, AvatarService avatarService, ApplicationProperties applicationProperties, User user) {
		this.avatarService = avatarService;
		this.searchProvider = searchProvider;
		this.applicationProperties = applicationProperties;
		this.user = user;
		this.dateFrom = dateFrom;
		PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(Configuration.KEY);
		this.configuration = new Configuration(pluginSettings.get("configuration"));
		this.projects = configuration.projectListFromSla(slas);
	}

	public void run() {
		try {
			
			JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
			Query query = queryBuilder.where().createdAfter(dateFrom).and().project().inNumbers(this.projects).buildQuery();
			log.error("query "+query.getWhereClause());
			this.total = searchProvider.searchCount(query, user);
			PagerFilter<Object> pager = new PagerFilter<Object>(10000);
			SearchResults results = searchProvider.search(query, user, pager);
			List<Issue> issues = results.getIssues();
			for (Issue issue : issues) {
				log.error("Found {} issue", issue.getKey());
				String url = applicationProperties.getBaseUrl();
				Runnable transport = new Transporter(url,this.configuration,issue, EventType.ISSUE_CREATED_ID, this.avatarService);
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
