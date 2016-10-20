/*
 * @EnvUtil.java 2016. 7. 15.
 *
 * Copyright NAVER Corp. All rights Reserved. 
 * NAVER PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sojw.ahnchangho.core.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.sojw.ahnchangho.core.config.EnvPropertyConfig;

/**
 * @author GD
 * @see EnvPropertyConfig
 */
public class EnvUtils {
	// spring container �뿉�꽌 二쇱엯 .
	private static final Map<String, String> ENV_MAP = new HashMap<String, String>();

	/**
	 * Inits the env.
	 *
	 * @param envMap the env map
	 */
	public static void initEnv(Map<String, String> envMap) {
		ENV_MAP.putAll(envMap);
	}

	/**
	 * Gets the value.
	 *
	 * @param key the key
	 * @return the value
	 */
	public static String getValue(String key) {
		return ENV_MAP.get(key);
	}

	/**
	 * Checks if is local.
	 *
	 * @return the boolean
	 */
	public static Boolean isLocal() {
		return StringUtils.equals(getValue("profile"), "local");
	}
}