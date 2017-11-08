package com.sojw.ahnchangho.core.line;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Strings;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class LineClient.
 */
@Slf4j
@Component
public class LineClient {
	private static final String LINE_NOTIFY_API_URL = "https://notify-api.line.me/api/notify";
	private static final String LINE_API_KEY = "dIX1BrjfvA48pXpSTgCMk1ieaBz9mQOTqCUOCOqndw8";

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * Send line msq.
	 *
	 * @param news the news
	 */
	public void send(String msg) {
		if (Strings.isNullOrEmpty(msg)) {
			return;
		}

		ResponseEntity<String> result = restTemplate.exchange(LINE_NOTIFY_API_URL, HttpMethod.POST, httpEntity(msg), String.class);
		log.debug("result = {}", result);
	}

	/**
	 * Http entity.
	 *
	 * @param msq the msq
	 * @return the http entity
	 */
	private HttpEntity<MultiValueMap<String, String>> httpEntity(String msq) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + LINE_API_KEY);

		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
		parameters.add("message", msq);

		return new HttpEntity<MultiValueMap<String, String>>(parameters, headers);
	}
}