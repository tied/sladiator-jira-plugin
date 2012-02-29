package lv.ebit.jira.plugins;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import lv.ebit.jira.plugins.ConfigModel.Configuration;

public class IssueListener implements InitializingBean, DisposableBean {
	private static final Logger log = LoggerFactory.getLogger(IssueListener.class);
	private final EventPublisher eventPublisher;
	private List<Long> validEventsList;
	private final PluginSettingsFactory pluginSettingsFactory;

	@EventListener
	public void onIssueEvent(IssueEvent issueEvent) {
		Long eventTypeId = issueEvent.getEventTypeId();
		Issue issue = issueEvent.getIssue();
		
		PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        Configuration configuration = new Configuration(pluginSettings.get(Configuration.KEY));
        
		if (this.validEventsList.contains(eventTypeId)) {
			log.error("May be interested in Issue {} event {}", issue.getKey(), eventTypeId);
			
			List<String> slaTokens = configuration.slaTokensForProject(issue.getProjectObject().getId());
			if (slaTokens.size() == 0) {
				log.error("but not included in any SLA");
			} else {
				for (int i = 0; i < slaTokens.size(); i++) {
					log.error("sla_token send "+slaTokens.get(i));
				}
			}
			
			
		} else {
			log.error("Not interested in Issue {} event {}", issue.getKey(), eventTypeId);
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param eventPublisher
	 *            injected {@code EventPublisher} implementation.
	 */
	public IssueListener(EventPublisher eventPublisher, PluginSettingsFactory pluginSettingsFactory) {
		this.eventPublisher = eventPublisher;
		this.pluginSettingsFactory = pluginSettingsFactory;
		
		this.validEventsList = new ArrayList<Long>();
		this.validEventsList.add(EventType.ISSUE_CREATED_ID);
		this.validEventsList.add(EventType.ISSUE_UPDATED_ID);
//		this.validEventsList.add(EventType.ISSUE_ASSIGNED_ID);
		this.validEventsList.add(EventType.ISSUE_RESOLVED_ID);
		this.validEventsList.add(EventType.ISSUE_CLOSED_ID);
//		this.validEventsList.add(EventType.ISSUE_COMMENTED_ID);
		this.validEventsList.add(EventType.ISSUE_REOPENED_ID);
		this.validEventsList.add(EventType.ISSUE_DELETED_ID);
		this.validEventsList.add(EventType.ISSUE_MOVED_ID);
//		this.validEventsList.add(EventType.ISSUE_WORKLOGGED_ID);
		this.validEventsList.add(EventType.ISSUE_WORKSTARTED_ID);
		this.validEventsList.add(EventType.ISSUE_WORKSTOPPED_ID);
		this.validEventsList.add(EventType.ISSUE_GENERICEVENT_ID);
//		this.validEventsList.add(EventType.ISSUE_COMMENT_EDITED_ID);
//		this.validEventsList.add(EventType.ISSUE_WORKLOG_UPDATED_ID);
//		this.validEventsList.add(EventType.ISSUE_WORKLOG_DELETED_ID);
		
	}

	/**
	 * Called when the plugin has been enabled.
	 * 
	 * @throws Exception
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// register ourselves with the EventPublisher
		eventPublisher.register(this);
	}

	/**
	 * Called when the plugin is being disabled or removed.
	 * 
	 * @throws Exception
	 */
	@Override
	public void destroy() throws Exception {
		// unregister ourselves with the EventPublisher
		eventPublisher.unregister(this);
	}
}
