package com.sojw.ahnchangho.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.input.BOMInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class ResourceLoaderUtil {
	@Autowired
	private ResourceLoader resourceLoader;

	/**
	 * Gets the input stream.
	 *
	 * @param filename the filename
	 * @return the input stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Reader getReader(String filename) throws IOException {
		Resource resource = resourceLoader.getResource(filename);
		if (resource == null) {
			return null;
		}
		return new BufferedReader(new InputStreamReader(new BOMInputStream(resource.getInputStream()), "UTF-8"));
	}

	/**
	 * Load file contents.
	 *
	 * @param filename the filename
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public List<String> loadFileContents(String filename) throws IOException {
		return loadFileContents(resourceLoader.getResource(filename));
	}

	/**
	 * Load file contents.
	 *
	 * @param resource the resource
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private List<String> loadFileContents(Resource resource) throws IOException {
		InputStream inputStream = resource.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		//		StringBuilder builder = new StringBuilder();
		List<String> builder = Lists.newArrayList();
		String line;
		while ((line = reader.readLine()) != null) {
			//			builder.append(line).append('\n');
			builder.add(line);
		}
		reader.close();

		return builder;
	}
}