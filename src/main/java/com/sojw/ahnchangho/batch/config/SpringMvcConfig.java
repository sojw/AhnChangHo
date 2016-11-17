package com.sojw.ahnchangho.batch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.sojw.ahnchangho.RootPackageMarker;

/**
 * The Class SpringMvcConfig.
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackageClasses = RootPackageMarker.class, useDefaultFilters = false, includeFilters = {
	@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Controller.class), @ComponentScan.Filter(type = FilterType.ANNOTATION, value = ControllerAdvice.class)})
public class SpringMvcConfig extends WebMvcConfigurerAdapter {
	@Bean
	public InternalResourceViewResolver internalResourceViewResolver() {
		return new InternalResourceViewResolver() {
			{
				setOrder(9);
				setPrefix("/WEB-INF/views/");
				setSuffix(".jsp");
			}
		};
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("forward:/home");
		registry.addViewController("/index.html").setViewName("forward:/home");
	}
}