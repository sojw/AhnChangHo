package com.sojw.ahnchangho.batch.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.sojw.ahnchangho.core.config.RootContextConfig;

/**
 * WebInitializer
 *
 * @author se.hyung@navercorp.com
 * @since 2016. 10. 18.
 */
public class WebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class[] {RootContextConfig.class, SpringBatchConfig.class};
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class[] {SpringMvcConfig.class};
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] {"/"};
	}
}