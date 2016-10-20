package com.sojw.ahnchangho.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;

@Configuration
public class MustacheConfig {
	@Bean
	public MustacheFactory mustacheFactory() {
		DefaultMustacheFactory defaultMustacheFactory = new DefaultMustacheFactory();
		return defaultMustacheFactory;
	}
}
