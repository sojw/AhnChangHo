package com.sojw.ahnchangho.batch.job.listener;

import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

import com.sojw.ahnchangho.batch.BatchConstants;

@Component
public class JobDuplicationListener extends JobExecutionListenerSupport {
	public static final ConcurrentHashMap<String, Long> RUNNING_JOB_NAMES = new ConcurrentHashMap<>();
	private static final Logger LOG = LoggerFactory.getLogger(JobDuplicationListener.class);

	@Override
	public void beforeJob(JobExecution jobExecution) {
		String jobName = jobExecution.getJobInstance().getJobName();
		String key = getKey(jobExecution);

		if (null == RUNNING_JOB_NAMES.get(key)) {
			LOG.info("Job start - {}", key);
			RUNNING_JOB_NAMES.put(key, jobExecution.getJobId());
		} else {
			LOG.info("Job - {} already executed!", jobName);
			jobExecution.stop();
		}
	}

	private String getKey(JobExecution jobExecution) {
		return String.format("%s::%s", jobExecution.getJobInstance().getJobName(), makeParameter(jobExecution));
	}

	private String makeParameter(JobExecution jobExecution) {
		StringJoiner buffer = new StringJoiner("&");
		Map<String, JobParameter> parameter = jobExecution.getJobParameters().getParameters();
		for (Map.Entry<String, JobParameter> entry : parameter.entrySet()) {
			if (BatchConstants.FIRE_TIME_KEY.equals(entry.getKey())) {
				continue;
			}
			buffer.add(String.format("%s=%s", entry.getKey(), entry.getValue()));
		}
		return buffer.toString();
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		String key = getKey(jobExecution);
		RUNNING_JOB_NAMES.remove(key);
		LOG.info("Job end - {}", key);
	}
}
