package com.sojw.ahnchangho.core.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.sojw.ahnchangho.RootPackageMarker;
import com.sojw.ahnchangho.core.util.ObjectMapperFactory;

@Configuration
@EnableScheduling
@ComponentScan(basePackageClasses = RootPackageMarker.class, useDefaultFilters = true, excludeFilters = {
	@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Controller.class), @ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class)})
@Import({HttpClientConfig.class, EhCacheConfig.class, MessageConfig.class, EnvPropertyConfig.class, SpringRetryConfig.class, MustacheConfig.class})
public class RootContextConfig implements ResourceLoaderAware {

	private ResourceLoader resourceLoader;

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Load file contents.
	 *
	 * @param filename the filename
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected String loadFileContents(String filename) throws IOException {
		return loadFileContents(resourceLoader.getResource(filename));
	}

	/**
	 * Load file contents.
	 *
	 * @param resource the resource
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected String loadFileContents(org.springframework.core.io.Resource resource) throws IOException {
		InputStream inputStream = resource.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line).append('\n');
		}
		reader.close();

		return builder.toString();
	}

	/**
	 * Object mapper.
	 *
	 * @return the object mapper
	 */
	@Bean
	public ObjectMapper objectMapper() {
		return ObjectMapperFactory.mapper();
	}

	/**
	 * Mustache factory.
	 *
	 * @return the mustache factory
	 */
	@Bean
	public MustacheFactory mustacheFactory() {
		DefaultMustacheFactory defaultMustacheFactory = new DefaultMustacheFactory();
		return defaultMustacheFactory;
	}

	/**
	 * Validator factory.
	 *
	 * @param messageSource the message source
	 * @return the validator
	 */
	@Bean
	public Validator validatorFactory(ReloadableResourceBundleMessageSource messageSource) {
		LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
		factory.setValidationMessageSource(messageSource);

		return factory;
	}

	//	/**
	//	 * Validation post processor.
	//	 *
	//	 * @param validator the validator
	//	 * @return the method validation post processor
	//	 */
	//	@Bean
	//	public MethodValidationPostProcessor validationPostProcessor(Validator validator) {
	//		MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
	//		processor.setValidator(validator);
	//		return processor;
	//	}

	/**
	 * Property config in dev.
	 *
	 * @return the property sources placeholder configurer
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
		propertySourcesPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
		propertySourcesPlaceholderConfigurer.setNullValue(StringUtils.EMPTY);
		return propertySourcesPlaceholderConfigurer;
	}

	/**
	 * Cache manager.
	 *
	 * @param ehCacheCacheManager the eh cache cache manager
	 * @param redisCacheManager the redis cache manager
	 * @return the cache manager
	 */
	@Bean
	@Primary
	public CacheManager cacheManager(EhCacheCacheManager ehCacheCacheManager) {
		CompositeCacheManager compositeCacheManager = new CompositeCacheManager(ehCacheCacheManager);
		return compositeCacheManager;
	}

	//	@Bean
	//	public ThreadPoolTaskExecutor taskExecutor() {
	//		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
	//		threadPoolTaskExecutor.setCorePoolSize(10);
	//		threadPoolTaskExecutor.setMaxPoolSize(20);
	//		threadPoolTaskExecutor.setQueueCapacity(5);
	//
	//		return threadPoolTaskExecutor;
	//	}
}