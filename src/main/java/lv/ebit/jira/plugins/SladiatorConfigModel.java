package lv.ebit.jira.plugins;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class SladiatorConfigModel {
	public static final String KEY = "lv.ebit.jira.plugins.sladiator";
	@XmlElement
	private String sla_token="";
	
	public String getSlaToken(){
		return this.sla_token;
	}
	
	public void setSlaToken(String value){
		this.sla_token = value;
	}
	
	@XmlElement
	private String send_assignee="0";
	
	public String getSendAssignee(){
		return this.send_assignee;
	}
	
	public void setSendAssignee(String value){
		this.send_assignee = value;
	}
	public boolean isSendAssignee() {
		return this.send_assignee.equals("1");
	}
	public String checkedSendAssignee(){
		if (this.isSendAssignee()) {
			return "checked=\"checked\"";
		} else {
			return "";
		}
	}
	
	@XmlElement
	private String project;
	
	public String getProject(){
		return this.project;
	}
	
	public void setProject(String value){
		this.project = value;
	}
	
	public boolean sendToSladiator() {
		return !this.getSlaToken().isEmpty();
	}
	private ArrayList<String> errors = new ArrayList<String>();
	
	public boolean isValid() {
		this.errors = new ArrayList<String>();
		if (this.getSlaToken().isEmpty()) {
			this.errors.add("SLA token is required");
		}
		if (this.getProject().toString().isEmpty()) {
			this.errors.add("Project is required");
		}
		return this.errors.isEmpty();
	}
	public ArrayList<String> getErrors() {
		return this.errors;
	}
	public String getErrorMessages() {
		return StringUtils.join(this.getErrors(), ".");
	}
	
	public String toString() {
		JSONObject json = new JSONObject(this);
		try {
			json.put("sla_token", this.getSlaToken());
			json.put("send_assignee", this.getSendAssignee());
			json.put("project", this.getProject());
		} catch (JSONException e) {
			SladiatorRestResource.log.error("Error in SladiatorConfigModel.toString:" + e.getMessage());
		}
		return json.toString();
	}
	public SladiatorConfigModel(Object config) {
		if (!(config == null)) {
			try {
				JSONObject json = new JSONObject(config.toString());
				this.setSlaToken(json.get("sla_token").toString());
				this.setSendAssignee(json.get("send_assignee").toString());
				this.setProject(json.get("project").toString());
			} catch (JSONException e) {
				SladiatorRestResource.log.error("Error in SladiatorConfigModel.SladiatorConfigModel:" + e.getMessage());
			}
		}
	}
	public SladiatorConfigModel() {
		
	}
}
