package com.sojw.ahnchangho.batch.job.dividenstock;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class DividendRecordSearchTaskletJob {
	public static final String JOB_NAME = "dividendRecordSearchTaskletJob";
	private static final String STEP_NAME = "dividendRecordSearchTaskletStep";

	@Bean
	public TaskletStep dividendRecordSearchTaskletStep(StepBuilderFactory stepBuilderFactory) throws Exception {
		return stepBuilderFactory.get(STEP_NAME).tasklet(dividendRecordSearchTasklet).build();
	}

	@Bean
	public Job dividendRecordSearchTaskletJobBuild(JobBuilderFactory jobBuilderFactory, TaskletStep dividendRecordSearchTaskletStep) throws Exception {
		return jobBuilderFactory.get(JOB_NAME).start(dividendRecordSearchTaskletStep).build();
	}

	@Autowired
	private DividendRecordSearchTasklet dividendRecordSearchTasklet;
}