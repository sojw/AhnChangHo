package com.sojw.ahnchangho.batch.job.stockalram;

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
public class StockalramTaskletJob {
	public static final String JOB_NAME = "stockalramTaskletJob";
	private static final String STEP_NAME = "stockalramTaskletStep";

	@Bean
	public TaskletStep stockalramTaskletStep(StepBuilderFactory stepBuilderFactory) throws Exception {
		return stepBuilderFactory.get(STEP_NAME).tasklet(stockalramTasklet).build();
	}

	@Bean
	public Job stockalramTaskletJobBuild(JobBuilderFactory jobBuilderFactory, TaskletStep stockalramTaskletStep) throws Exception {
		return jobBuilderFactory.get(JOB_NAME).start(stockalramTaskletStep).build();
	}

	@Autowired
	private StockalramTasklet stockalramTasklet;
}