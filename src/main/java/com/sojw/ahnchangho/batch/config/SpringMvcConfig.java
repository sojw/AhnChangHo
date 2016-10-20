package com.sojw.ahnchangho.batch.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.sojw.ahnchangho.RootPackageMarker;

/**
 * SpringMvcConfig
 *
 * @author se.hyung@navercorp.com
 * @since 2016. 10. 18.
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackageClasses = RootPackageMarker.class, useDefaultFilters = false, includeFilters = {
	@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Controller.class), @ComponentScan.Filter(type = FilterType.ANNOTATION, value = ControllerAdvice.class)})
public class SpringMvcConfig extends WebMvcConfigurerAdapter {
}
