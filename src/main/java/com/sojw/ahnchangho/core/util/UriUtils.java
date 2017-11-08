/* 
* UriUtil.java 2016. 8. 11. 
* 
* Copyright 2016 NAVER Corp. All rights Reserved. 
* NAVER PROPRIETARY/CONFIDENTIAL. Use is subject to license terms. 
*/
package com.sojw.ahnchangho.core.util;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;

/**
 * The Class UriUtils.
 */
public class UriUtils {

	/**
	 * Of.
	 *
	 * @param url the url
	 * @param queryParameters the query parameters
	 * @return the uri
	 */
	public static URI of(String url, Map<String, String> queryParameters) {
		if (Strings.isNullOrEmpty(url)) {
			return null;
		}

		return UriComponentsBuilder.fromHttpUrl(url).queryParams(multiValueMap(queryParameters)).build().encode().toUri();
	}

	/**
	 * Of.
	 *
	 * @param url the url
	 * @param pathVariables the path variables
	 * @param queryParameters the query parameters
	 * @return the uri
	 */
	public static URI of(String url, Map<String, String> pathVariables, Map<String, String> queryParameters) {
		if (Strings.isNullOrEmpty(url)) {
			return null;
		}

		return UriComponentsBuilder.fromHttpUrl(url).queryParams(multiValueMap(queryParameters)).buildAndExpand(pathVariables).encode().toUri();
	}

	/**
	 * Multi value map.
	 *
	 * @param map the map
	 * @return the multi value map
	 */
	public static MultiValueMap<String, String> multiValueMap(Map<String, String> map) {
		if (MapUtils.isEmpty(map)) {
			return null;
		}

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			params.add(entry.getKey(), entry.getValue());
		}
		return params;
	}

	/**
	 * Multi value map.
	 *
	 * @param <T> the generic type
	 * @param request the request
	 * @return the string
	 */
	public static <T> MultiValueMap<String, String> multiValueMap(final T request) {
		if (request == null) {
			return null;
		}

		Map<String, Object> requestMap = JsonUtils.convertValue(request, new TypeReference<Map<String, Object>>() {
		});
		if (MapUtils.isEmpty(requestMap)) {
			return null;
		}

		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
		for (Entry<String, Object> entry : requestMap.entrySet()) {
			if (entry.getValue() instanceof List) {
				((List)entry.getValue()).forEach(each -> {
					parameters.add(entry.getKey(), each.toString());
				});
			} else {
				parameters.add(entry.getKey(), entry.getValue().toString());
			}
		}

		return parameters;
	}

	/**
	 * Url with param.
	 *
	 * @param requestURL the request URL
	 * @param queryString the query string
	 * @return the string
	 */
	public static String of(final String requestURL, final String queryString) {
		final String url = StringUtils.defaultIfEmpty(requestURL, StringUtils.EMPTY).toString();
		if (Strings.isNullOrEmpty(url)) {
			return StringUtils.EMPTY;
		}

		return url + (queryString == null ? StringUtils.EMPTY : "?" + queryString);
	}
}