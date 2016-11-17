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
	public TaskletStep sampleTaskletStep(StepBuilderFactory stepBuilderFactory) throws Exception {
		return stepBuilderFactory.get(STEP_NAME).tasklet(sampleTasklet).build();
	}

	@Bean
	public Job sampleTaskletJob(JobBuilderFactory jobBuilderFactory, TaskletStep sampleTaskletStep) throws Exception {
		return jobBuilderFactory.get(JOB_NAME).start(sampleTaskletStep).build();
	}

	@Autowired
	private StockalramTasklet sampleTasklet;
}