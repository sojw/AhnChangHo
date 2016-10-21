package com.sojw.ahnchangho.batch.config;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.sojw.ahnchangho.batch.job.dividenstock.DividenStockJobConfig;

/**
 * SpringBatchConfig
 *
 * @author se.hyung@navercorp.com
 * @since 2016. 10. 18.
 *
 * job이 추가될 때 클래스를 job 설정 클래스를 Import 해야함.
 */
@Configuration
@ComponentScan(basePackages = "com.sojw.ahnchangho.batch.job")
@Import({DividenStockJobConfig.class})
public class SpringBatchConfig {
	@Bean
	public JobRegistry jobRegistry() {
		return new MapJobRegistry();
	}

	/*
	 * Spring 이 제공하는 주요 애노테이션 활성화(@Required, @Autowired)
	 */
	@Bean
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
		return new JobRegistryBeanPostProcessor() {
			{
				setJobRegistry(jobRegistry);
			}
		};
	}

	@Bean
	public SimpleJobLauncher simpleJobLauncher(JobRepository jobRepository) throws Exception {
		return new SimpleJobLauncher() {
			{
				setJobRepository(jobRepository);
			}
		};
	}

	//메모리
	@Bean
	public JobRepository jobRepository() throws Exception {
		MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean();
		factory.afterPropertiesSet();
		return (JobRepository)factory.getObject();
	}

	//물리 디비
	//	@Bean
	//	public JobRepository jobRepository(DataSource batchDataSource, PlatformTransactionManager batchTransactionManager) throws Exception {
	//		JobRepositoryFactoryBean fb = new JobRepositoryFactoryBean();
	//		fb.setDataSource(batchDataSource);
	//		fb.setTransactionManager(batchTransactionManager);
	//		return fb.getObject();
	//	}
}