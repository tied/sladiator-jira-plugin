package lv.ebit.jira.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.base.Joiner;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import lv.ebit.jira.plugins.ConfigModel.Configuration;

public class IssueListener implements InitializingBean, DisposableBean {
	private final EventPublisher eventPublisher;
	private final ApplicationProperties applicationProperties;
	private List<Long> validEventsList;
	private static PluginSettingsFactory pluginSettingsFactory;
	private final AvatarService avatarService;

	@EventListener
	public void onIssueEvent(IssueEvent issueEvent) {
		Long eventTypeId = issueEvent.getEventTypeId();
		Issue issue = issueEvent.getIssue();
		
		PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(Configuration.KEY);
		Configuration configuration = new Configuration(pluginSettings.get("configuration"));
		
		if (this.validEventsList.contains(eventTypeId)) {
			String url = applicationProperties.getBaseUrl()+"/browse/";
			Runnable transport = new Transporter(url,configuration,issue, eventTypeId, this.avatarService);
			new Thread(transport).start();
		}
	}
	
	public static void addFailedIssue(String key) {
		PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(Configuration.KEY);
		synchronized(pluginSettings) {
			List<String> errors = new ArrayList<String>(); 
	        if (pluginSettings.get("errors") != null) {
	        	errors = new ArrayList<String>(Arrays.asList(pluginSettings.get("errors").toString().split(",")));
	        	errors.remove("");
	        }
	        errors.remove(key);
	        errors.add(key);
			pluginSettings.put("errors",Joiner.on(",").skipNulls().join(errors));
		}
	}
	public static void removeFailedIssue(String key) {
		PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(Configuration.KEY);
		synchronized(pluginSettings) {
			List<String> errors = new ArrayList<String>(); 
	        if (pluginSettings.get("errors") != null) {
	        	errors = new ArrayList<String>(Arrays.asList(pluginSettings.get("errors").toString().split(",")));
	        	errors.remove("");
	        }
			errors.remove(key);
			pluginSettings.put("errors",Joiner.on(",").skipNulls().join(errors));
		}
	}
	/**
	 * Constructor.
	 * 
	 * @param eventPublisher
	 *            injected {@code EventPublisher} implementation.
	 */
	public IssueListener(EventPublisher eventPublisher, PluginSettingsFactory pluginSettingsFactory, ApplicationProperties applicationProperties, AvatarService avatarService) {
		this.avatarService = avatarService;
		this.eventPublisher = eventPublisher;
		IssueListener.pluginSettingsFactory = pluginSettingsFactory;
		this.applicationProperties = applicationProperties;
		
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
