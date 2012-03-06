package lv.ebit.jira.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lv.ebit.jira.plugins.ConfigModel.Configuration;

import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.util.collect.MapBuilder;

import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

public class Transporter implements Runnable {
	public static final Logger log = LoggerFactory.getLogger(Transporter.class);
	private Configuration configuration;
	private Issue issue;
	private String url;
	private Long eventTypeId;
	private AvatarService avatarService;
	private static String realSlaUrl = "http://172.17.1.111:3000/api/tickets/";

	public Transporter(String url, Configuration configuration, Issue issue, Long eventTypeId, AvatarService avatarService) {
		this.configuration = configuration;
		this.issue = issue;
		this.url = url;
		this.eventTypeId = eventTypeId;
		this.avatarService = avatarService;
	}

	@Override
	public void run() {
		IssueListener.removeFailedIssue(issue.getKey());
		List<String> slaTokens = configuration.slaTokensForProject(issue.getProjectObject().getId());
		if (slaTokens.size() > 0) {
			for (int i = 0; i < slaTokens.size(); i++) {
				log.info("Send issue {} to sla {}", issue.getKey(), slaTokens.get(i));
				JSONObject json;
				try {
					json = collectIssueInfo(issue, configuration.getConfig().get(slaTokens.get(i)).isSendAssignee());
					if (!json.has("error") && sendData(slaTokens.get(i), json.toString())) {
						log.info("Issue {} was sent", issue.getKey());
					} else {
						IssueListener.addFailedIssue(issue.getKey());
						log.error("Issue {} was not sent", issue.getKey());
					}
				} catch (JSONException e) {
					log.error("Issue {} was not collected", issue.getKey());
				}
			}
		}
	}

	public JSONObject collectIssueInfo(Issue issue, boolean collectAssignee) throws JSONException {
		JSONObject json = new JSONObject();
		
			json.putOpt("issue_key", issue.getKey());
			json.putOpt("created_at", issue.getCreated());
			json.putOpt("updated_at", issue.getUpdated());
			json.putOpt("due_date", issue.getDueDate());
			json.putOpt("priority", issue.getPriorityObject().getName());
			json.putOpt("issue_type", issue.getIssueTypeObject().getName());
			json.putOpt("status", issue.getStatusObject().getName());
			json.putOpt("project", issue.getProjectObject().getKey());
			json.putOpt("issue_url", url + issue.getKey());
			
			// Iterator<ProjectComponent> component =
			// issue.getComponentObjects().iterator();
			// List<String> components = new ArrayList<String>();
			// while (component.hasNext()) {
			// components.add(component.next().getName());
			// }
			// json.putOpt("components", components);
			if (collectAssignee) {
				User assignee = issue.getAssigneeUser();
				if (assignee != null) {
					json.putOpt("assignee", assignee.getDisplayName());
					json.putOpt("assignee_email", assignee.getEmailAddress());
					json.putOpt("assignee_avatar_url", this.avatarService.getAvatarURL(assignee, assignee.getName(), Avatar.Size.LARGE));
				}
			}
			
			if (issue.getResolutionObject() != null) {
				json.putOpt("resolution", issue.getResolutionObject().getName());
				json.putOpt("resolution_date", issue.getResolutionDate());
			}
			json.putOpt("transitions", getTransitions(issue));
		return json;
	}

	public List<JSONObject> getTransitions(Issue issue) throws JSONException {
		OfBizDelegator delegator = new DefaultOfBizDelegator(CoreFactory.getGenericDelegator());
		Map<String, Long> params = MapBuilder.build("issue", issue.getId());
		List<GenericValue> changeGroups = delegator.findByAnd("ChangeGroup", params);

		List<JSONObject> retList = new ArrayList<JSONObject>();

		for (GenericValue changeGroup : changeGroups) {
			Map<String, ? extends Object> paramsItem = MapBuilder.build("group", changeGroup.getLong("id"), "field", "status", "fieldtype", "jira");

			List<GenericValue> changeItems = delegator.findByAnd("ChangeItem", paramsItem);
			for (GenericValue changeItem : changeItems) {
				JSONObject json = new JSONObject();
				json.put("created", changeGroup.getTimestamp("created"));
				json.put("status_to", ComponentAccessor.getConstantsManager().getStatusObject(changeItem.getString("newvalue")).getName());
				json.put("status_from", ComponentAccessor.getConstantsManager().getStatusObject(changeItem.getString("oldvalue")).getName());
				retList.add(json);
				// Deprecated. Use ComponentAccessor instead. Since v4.4.
				// ManagerFactory.getConstantsManager().getStatusObject(changeItem.getString("newvalue"));
			}

		}
		return retList;
	}

	public boolean sendData(String slaToken, String body) {
		HttpClient client = new HttpClient();
		int statusCode = 0;
		try {
			if (eventTypeId.equals(EventType.ISSUE_CREATED_ID)) {
				PostMethod httpMethod = new PostMethod(Transporter.realSlaUrl);
				httpMethod.setRequestHeader("Content-Type", "application/json");
				httpMethod.setRequestHeader("SLA_TOKEN", slaToken);

				httpMethod.setRequestEntity(new StringRequestEntity(body, "application/json", null));
				statusCode = client.executeMethod(httpMethod);
			} else if (eventTypeId.equals(EventType.ISSUE_DELETED_ID)) {
				DeleteMethod httpMethod = new DeleteMethod(Transporter.realSlaUrl + issue.getKey());
				httpMethod.setRequestHeader("Content-Type", "application/json");
				httpMethod.setRequestHeader("SLA_TOKEN", slaToken);
				
				statusCode = client.executeMethod(httpMethod);
			} else {
				PutMethod httpMethod = new PutMethod(Transporter.realSlaUrl + issue.getKey());
				httpMethod.setRequestHeader("Content-Type", "application/json");
				httpMethod.setRequestHeader("SLA_TOKEN", slaToken);
				
				httpMethod.setRequestEntity(new StringRequestEntity(body, "application/json", null));
				statusCode = client.executeMethod(httpMethod);
			}
		} catch (HttpException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		if (statusCode != 202) {
			log.error("statusCode was "+statusCode);
			return false;
		}
		return true;
	}
}
