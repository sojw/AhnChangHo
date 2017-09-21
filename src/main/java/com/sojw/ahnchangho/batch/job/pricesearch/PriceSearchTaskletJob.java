/**
 * 
 */
package com.sojw.ahnchangho.batch.job.pricesearch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Naver
 *
 */
@Configuration
@EnableBatchProcessing
public class PriceSearchTaskletJob {
	public static final String JOB_NAME = "priceSearchTaskletJob";
	private static final String STEP_NAME = "priceSearchTaskletStep";

	@Bean
	public TaskletStep priceSearchTaskletStep(StepBuilderFactory stepBuilderFactory) throws Exception {
		return stepBuilderFactory.get(STEP_NAME).tasklet(priceSearchTaskletStep).build();
	}

	@Bean
	public Job priceSearchTaskletJobBuild(JobBuilderFactory jobBuilderFactory, TaskletStep priceSearchTaskletStep) throws Exception {
		return jobBuilderFactory.get(JOB_NAME).start(priceSearchTaskletStep).build();
	}

	@Autowired
	private PriceSearchTasklet priceSearchTaskletStep;
}