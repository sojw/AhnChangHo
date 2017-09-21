package com.sojw.ahnchangho.batch.job.dividenstock;

import java.util.List;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sojw.ahnchangho.core.model.CompanyInfo;

@Configuration
@EnableBatchProcessing
public class DividenStockJobConfig {
	public static final String JOB_NAME = "dividenStockJob";
	private static final String STEP_NAME = "dividenStockStep";

	@Autowired
	private ItemReader<List<CompanyInfo>> dividenStockItemReader;

	@Autowired
	private ItemWriter<Map<String, Double>> dividenStockItemWriter;

	@Autowired
	private ItemProcessor<? super List<CompanyInfo>, ? extends Map<String, Double>> dividenStockItemProcessor;

	@Bean
	public TaskletStep dividenStockStep(StepBuilderFactory stepBuilderFactory) throws Exception {
		return stepBuilderFactory.get(STEP_NAME).<List<CompanyInfo>, Map<String, Double>> chunk(1).reader(dividenStockItemReader).processor(dividenStockItemProcessor).writer(
			dividenStockItemWriter).build();
	}

	@Bean
	public Job dividenStockJob(JobBuilderFactory jobBuilderFactory, TaskletStep dividenStockStep) throws Exception {
		return jobBuilderFactory.get(JOB_NAME).start(dividenStockStep).build();
	}
}