package lv.ebit.jira.plugins;

import java.util.List;

import lv.ebit.jira.plugins.ConfigModel.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.issue.Issue;

public class Transporter implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(Transporter.class);
	private Configuration configuration;
	private Issue issue;

	public Transporter(Configuration configuration, Issue issue) {
		this.configuration = configuration;
		this.issue = issue;
	}

	@Override
	public void run() {
		log.error("start transporter");
		List<String> slaTokens = configuration.slaTokensForProject(issue.getProjectObject().getId());
		if (slaTokens.size() > 0) {
			for (int i = 0; i < slaTokens.size(); i++) {
				log.info("Prepare SLA for token=" + slaTokens.get(i));
				String output = collectIssueInfo(slaTokens.get(i),issue);
				log.error(output);
			}
		}
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.error("stop transporter");
	}
	
	public String collectIssueInfo(String slaToken, Issue issue) {
		JSONObject json = new JSONObject();
		try {
			json.putOpt("token", slaToken);
			json.putOpt("issue_key", issue.getKey());
			json.putOpt("created_at", issue.getCreated());
			json.putOpt("updated_at", issue.getUpdated());
			json.putOpt("due_date", issue.getDueDate());
			json.putOpt("priority", issue.getPriorityObject().getName());
			json.putOpt("issue_type", issue.getIssueTypeObject().getName());
			json.putOpt("status", issue.getStatusObject().getName());
			json.putOpt("project", issue.getProjectObject().getKey());
//			issue_link
//			issue.getComponentObjects()
//			issue.get
			if (issue.getResolutionObject() != null) {
				json.putOpt("resolution", issue.getResolutionObject().getName());
				json.putOpt("resolution_date", issue.getResolutionDate());
			}
			
		} catch (JSONException e) {
			log.error(e.getMessage());
			log.error(e.getStackTrace().toString());
		}
		return json.toString();
	}
}
