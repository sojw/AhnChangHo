/*
 * @EnvPropertyConfig.java 2016. 7. 15.
 *
 * Copyright NAVER Corp. All rights Reserved. 
 * NAVER PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sojw.ahnchangho.core.config;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.sojw.ahnchangho.core.util.EnvUtils;

/**
 * @author GD
 * 
 */
@Configuration
@ImportResource("classpath:META-INF/env-properties.xml")
public class EnvPropertyConfig {
	@Value("#{env}")
	private Map<String, String> envPropertyMap;

	@PostConstruct
	public void initEnv() {
		EnvUtils.initEnv(envPropertyMap);
	}
}