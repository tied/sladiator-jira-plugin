package lv.ebit.jira.plugins;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.DeleteMethod;


public class SladiatorTransportDelete implements Runnable {
	
	private SladiatorConfigModel config;
	private String issueKey;
	
	public SladiatorTransportDelete(SladiatorConfigModel config, String issueKey){
		this.config = config;
		this.issueKey = issueKey;
	}

	@Override
	public void run() {
		HttpClient client = new HttpClient();
		int statusCode = 0;
		try {
			DeleteMethod httpMethod = new DeleteMethod(SladiatorIssueListener.getServiceUrl() + "/api/tickets/" + this.issueKey);
			httpMethod.setRequestHeader("Content-Type", "application/json");
			httpMethod.setRequestHeader("X-SLA-Token", this.config.getSlaToken());

			statusCode = client.executeMethod(httpMethod);
		} catch (HttpException e) {
			SladiatorTransport.log.error("HttpException:"+e.getMessage());
			SladiatorTransport.log.error(e.getStackTrace().toString());
		} catch (IOException e) {
			SladiatorTransport.log.error("IOException"+e.getMessage());
		} catch (Exception e) {
			SladiatorTransport.log.error("Exception"+e.getMessage());
		}
		if (statusCode != 200) {
			SladiatorTransport.log.error("statusCode was " + statusCode);
		}
		
	}
}
