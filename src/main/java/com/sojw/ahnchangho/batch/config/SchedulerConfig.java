package com.sojw.ahnchangho.batch.config;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sojw.ahnchangho.batch.BatchConstants;
import com.sojw.ahnchangho.batch.job.srtalram.SrtalramTaskletJob;
import com.sojw.ahnchangho.batch.job.stockalram.StockalramTaskletJob;

@Component
public class SchedulerConfig {
	private static final Logger LOG = LoggerFactory.getLogger(SchedulerConfig.class);
	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	@Qualifier("simpleJobLauncher")
	private JobLauncher jobLauncher;

	//	@Scheduled(cron = "* * * * * *")
	//	public void cronTest1() {
	//		LOG.info("매초 호출이 됩니다 ");
	//	}

	//	@Scheduled(cron = "0 * * * * *")
	//	public void cronTest2() {
	//		LOG.info("매분 호출이 됩니다 ");
	//	}

	@Scheduled(cron = "*/7 * 07-20 * * *")
	public void stockalramTaskletJob() {
		LOG.info("stockalramTaskletJob start.");

		try {
			Job job = jobRegistry.getJob(StockalramTaskletJob.JOB_NAME);
			JobExecution jobExec = jobLauncher.run(job, getJobParameters());
			String result = makeLog(jobExec);
			LOG.info("result = {}", result);
		} catch (Exception e) {
			LOG.error("jobLaunch error jobName = {}", StockalramTaskletJob.JOB_NAME, e);
		}

		LOG.info("stockalramTaskletJob end.");
	}

//	@Scheduled(cron = "0/3 * * * * *")
//	public void srtalramTaskletJob() {
//		LOG.info("SrtalramTaskletJob start.");
//
//		try {
//			Job job = jobRegistry.getJob(SrtalramTaskletJob.JOB_NAME);
//			JobExecution jobExec = jobLauncher.run(job, getJobParameters());
//			String result = makeLog(jobExec);
//			LOG.info("result = {}", result);
//		} catch (Exception e) {
//			LOG.error("jobLaunch error jobName = {}", StockalramTaskletJob.JOB_NAME, e);
//		}
//
//		LOG.info("stockalramTaskletJob end.");
//	}

	/**
	 * Gets the job parameters.
	 *
	 * @return the job parameters
	 */
	private JobParameters getJobParameters() {
		JobParametersBuilder builder = new JobParametersBuilder();
		builder.addLong(BatchConstants.FIRE_TIME_KEY, (new Date()).getTime());
		return builder.toJobParameters();
	}

	/**
	 * Make log.
	 *
	 * @param jobExec the job exec
	 * @return the string
	 */
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