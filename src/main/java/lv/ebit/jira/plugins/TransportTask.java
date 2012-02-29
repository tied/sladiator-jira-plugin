package lv.ebit.jira.plugins;

import java.util.Map;

import com.atlassian.sal.api.scheduling.PluginJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransportTask implements PluginJob {
	private static final Logger log = LoggerFactory.getLogger(TransportTask.class);

	@Override
	public void execute(Map<String, Object> jobDataMap) {
		log.error("running in scheduler AAAAAAAAAAAAAAAAA");
	}
}
