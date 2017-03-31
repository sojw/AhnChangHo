package com.sojw.ahnchangho.core.config;

import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class MessageConfig {

	/**
	 * Message source.
	 *
	 * @return the reloadable resource bundle message source
	 */
	@Bean
	public ReloadableResourceBundleMessageSource messageSource() {
		ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
		source.setDefaultEncoding(StandardCharsets.UTF_8.toString());
		source.setCacheSeconds(0);
		source.setUseCodeAsDefaultMessage(true);

		String[] messageSourceBaseNames = {"classpath:META-INF/messages/"};
		source.setBasenames(messageSourceBaseNames);

		return source;
	}

	/**
	 * Message source accessor.
	 *
	 * @return the message source accessor
	 */
	@Bean
	public MessageSourceAccessor messageSorceAccessor() {
		return new MessageSourceAccessor(messageSource());
	}
}