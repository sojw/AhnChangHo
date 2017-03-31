package com.sojw.ahnchangho.core.config;

import java.nio.charset.StandardCharsets;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
@ImportResource("classpath:META-INF/env-properties.xml")
public class HttpClientConfig {

	@Value("#{new Integer(env.httpSocketTimeout)}")
	private int socketTimeout;

	@Value("#{new Integer(env.httpConnectTimeout)}")
	private int connectTimeout;

	/**
	 * Rest template.
	 *
	 * @return the rest template
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate() {
			{
				setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient()));
				getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
			}
		};
	}

	/**
	 * Http client.
	 *
	 * @return the closeable http client
	 */
	@Bean
	public CloseableHttpClient httpClient() {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(3, true));
		httpClientBuilder.setConnectionManager(connectionManager());
		httpClientBuilder.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(socketTimeout).build());
		httpClientBuilder.setDefaultRequestConfig(RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).build());
		httpClientBuilder.disableCookieManagement();
		return httpClientBuilder.build();
	}

	/**
	 * Connection manager.
	 *
	 * @return the pooling http client connection manager
	 */
	@Bean
	public PoolingHttpClientConnectionManager connectionManager() {
		return new PoolingHttpClientConnectionManager() {
			{
				setMaxTotal(200);
				setDefaultMaxPerRoute(50);
			}
		};
	}
}