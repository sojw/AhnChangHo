package com.sojw.ahnchangho.batch.job.srtalram;

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
public class SrtalramTaskletJob {
	public static final String JOB_NAME = "srtalramTaskletJob";
	private static final String STEP_NAME = "srtalramTaskletStep";

	@Autowired
	private SrtalramTasklet srtalramTasklet;

	@Bean
	public TaskletStep srtalramTaskletStep(StepBuilderFactory stepBuilderFactory) throws Exception {
		return stepBuilderFactory.get(STEP_NAME).tasklet(srtalramTasklet).build();
	}

	@Bean
	public Job srtalramTaskletJobBuild(JobBuilderFactory jobBuilderFactory, TaskletStep srtalramTaskletStep) throws Exception {
		return jobBuilderFactory.get(JOB_NAME).start(srtalramTaskletStep).build();
	}
}