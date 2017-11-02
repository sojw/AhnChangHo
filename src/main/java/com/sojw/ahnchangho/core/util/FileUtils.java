package com.sojw.ahnchangho.core.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The Class FileUtils.
 */
public class FileUtils {

	/**
	 * Current relative root path.
	 *
	 * @return the string
	 */
	public static String currentRelativeRootPath() {
		return currentRelativePath("");
	}

	/**
	 * Current relative path.
	 *
	 * @param path the path
	 * @return the string
	 */
	public static String currentRelativePath(String path) {
		Path currentRelativePath = Paths.get(path);
		return currentRelativePath.toAbsolutePath().toString();
	}
}