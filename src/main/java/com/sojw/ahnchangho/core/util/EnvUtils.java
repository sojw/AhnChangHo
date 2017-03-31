package com.sojw.ahnchangho.core.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class EnvUtils {
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