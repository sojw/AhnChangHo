package com.sojw.ahnchangho.batch.controller;

import java.util.Date;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.MDC;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sojw.ahnchangho.batch.BatchConstants;


@Controller
public class JobController {
	private static final Logger LOG = LoggerFactory.getLogger(JobController.class);
	private static final String ENCODING = "UTF-8";

	@RequestMapping("/jobLaunch/{jobName}")
	public void jobLaunch(@PathVariable(value = "jobName") String jobName, HttpServletRequest request, HttpServletResponse response) {
		try {
			ServletOutputStream outStream = response.getOutputStream();
			MDC.put(BatchConstants.RESPONSE_OUTPUT_STREAM, outStream);

			Job job = jobRegistry.getJob(jobName);
			JobExecution jobExec = jobLauncher.run(job, getJobParameters(request));

			IOUtils.write(makeLog(jobExec), outStream, ENCODING);
		} catch (Exception e) {
			LOG.error("jobLaunch error jobName = {}", jobName, e);
		} finally {
			MDC.remove(BatchConstants.RESPONSE_OUTPUT_STREAM);
		}
	}

	private JobParameters getJobParameters(HttpServletRequest request) {
		JobParametersBuilder builder = new JobParametersBuilder();
		builder.addLong(BatchConstants.FIRE_TIME_KEY, (new Date()).getTime());

		Map<String, String[]> param = request.getParameterMap();
		for (Map.Entry<String, String[]> entry : param.entrySet()) {
			builder.addString(entry.getKey(), entry.getValue()[0]);
		}
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

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	@Qualifier("simpleJobLauncher")
	private JobLauncher jobLauncher;

}
