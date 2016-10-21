package com.sojw.ahnchangho;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.sojw.ahnchangho.batch.job.dividenstock.DividenStockJobConfig;

public class BatchTestExecutor extends TestApplicationContext {
	private static final String FIRE_TIME_KEY = "schedule.scheduledFireTime";

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	@Qualifier("simpleJobLauncher")
	private JobLauncher syncJobLauncher;

	@Test
	public void test() throws NoSuchJobException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		Job job = jobRegistry.getJob(DividenStockJobConfig.JOB_NAME);
		JobExecution jobExec = syncJobLauncher.run(job, jobParameters());
		makeLog(jobExec);

		LOG.debug("application context load done.");
	}

	private JobParameters jobParameters() {
		JobParametersBuilder builder = new JobParametersBuilder();
		builder.addLong(FIRE_TIME_KEY, (new Date()).getTime());
		return builder.toJobParameters();
	}

	private String makeLog(JobExecution jobExec) {
		StringBuilder log = new StringBuilder();
		log.append("Job Execution finished");
		log.append(jobExec.toString());
		if (StringUtils.isNotBlank(jobExec.getExitStatus().getExitDescription())) {
			log.append(String.format("ExitDescription is %s", jobExec.getExitStatus().getExitDescription()));
		}
		return log.toString();
	}
}