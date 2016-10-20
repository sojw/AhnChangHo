package com.sojw.ahnchangho.batch.job.dividenstock;

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

import com.sojw.ahnchangho.core.model.DividenStock;

@Configuration
@EnableBatchProcessing
public class DividenStockJob {
	@Autowired
	private ItemReader<String> dividenStockItemReader;

	@Autowired
	private ItemWriter<DividenStock> dividenStockItemWriter;

	@Autowired
	private ItemProcessor<? super String, ? extends DividenStock> dividenStockItemProcessor;

	//	@Bean
	//	public DividenStockItemProcessor dividenStockItemProcessor() {
	//		return new DividenStockItemProcessor();
	//	}

	/*
	 * step 샘플
	 */
	@Bean
	public TaskletStep dividenStockStep(StepBuilderFactory stepBuilderFactory) throws Exception {
		return stepBuilderFactory.get("DividenStockStep").<String, DividenStock> chunk(1).reader(dividenStockItemReader).processor(dividenStockItemProcessor).writer(
			dividenStockItemWriter).build();
	}

	/*
	 * job 샘플
	 */
	@Bean
	public Job dividenStockJob(JobBuilderFactory jobBuilderFactory, TaskletStep dividenStockStep) throws Exception {
		return jobBuilderFactory.get("DividenStockJob").start(dividenStockStep).build();
	}
}