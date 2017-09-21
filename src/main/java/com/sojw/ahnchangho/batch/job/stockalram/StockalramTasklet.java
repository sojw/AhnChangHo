package com.sojw.ahnchangho.batch.job.stockalram;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
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
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.sojw.ahnchangho.batch.job.stockalram.SearchResult.ListItem;

@Component
public class StockalramTasklet implements Tasklet {
	private static final Logger LOG = LoggerFactory.getLogger(StockalramTasklet.class);
	private static final String DART_API_URL = "http://dart.fss.or.kr/api/search.json?auth=%s&page_set=50&start_dt=%s&dsp_tp=A&dsp_tp=B&dsp_tp=D&dsp_tp=F&dsp_tp=I&dsp_tp=J";
	private static final String DART_API_KEY = "62d77fc171e7f887beac19fc86b4d20df1337be3";
	private static final String LINE_API_KEY = "dIX1BrjfvA48pXpSTgCMk1ieaBz9mQOTqCUOCOqndw8";
	private static final String SAVE_FILE_FORMATT = "C:\\Users\\Naver\\Desktop\\stock_alram\\stock_alram_%s-%s-%s.txt";

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		LocalDate now = LocalDate.now();
		final String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		LOG.debug("date = {}", date);

		try {
			final SearchResult result = restTemplate.getForObject(String.format(DART_API_URL, DART_API_KEY, date), SearchResult.class);
			if (!StringUtils.equalsIgnoreCase(result.getErrCode(), "000") || CollectionUtils.isEmpty(result.getList())) {
				LOG.error("error!!!", result);
				return RepeatStatus.FINISHED;
			}

			final List<ListItem> news = getNews(result, readLastAlramItem(now));
			LOG.debug("news.size() = {}", news.size());

			saveLastAlramItem(now, news);
			final String msg = msg(news);
			LOG.debug("msg = {}", msg);
			sendLineMsq(msg);
		} catch (Exception e) {
			LOG.error("", e);
		}

		return RepeatStatus.FINISHED;
	}

	/**
	 * Send line msq.
	 *
	 * @param news the news
	 */
	private void sendLineMsq(String msg) {
		if (Strings.isNullOrEmpty(msg)) {
			return;
		}

		ResponseEntity<String> result = restTemplate.exchange("https://notify-api.line.me/api/notify", HttpMethod.POST, httpEntity(msg), String.class);
		LOG.debug("result = {}", result);
	}

	/**
	 * Msg.
	 *
	 * @param news the news
	 * @return the string
	 */
	private String msg(List<ListItem> news) {
		StringBuilder sb = new StringBuilder();
		for (ListItem item : news) {
			sb.append("[" + item.getCrpNm() + "] " + item.getRptNm() + " " + "http://m.dart.fss.or.kr/html_mdart/MD1007.html?rcpNo=" + item.getRcpNo() + "\r\n\r\n");
		}
		return sb.toString();
	}

	/**
	 * Http entity.
	 *
	 * @param msq the msq
	 * @return the http entity
	 */
	private HttpEntity httpEntity(String msq) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + LINE_API_KEY);
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
		parameters.add("message", msq);
		HttpEntity httpEntity = new HttpEntity(parameters, headers);
		return httpEntity;
	}

	/**
	 * Gets the news.
	 *
	 * @param result the result
	 * @param lastAlramItem the last alram item
	 * @return the news
	 */
	private List<ListItem> getNews(final SearchResult result, final String lastAlramItem) {
		List<ListItem> news = Lists.newArrayList();
		for (ListItem item : result.getList()) {
			List<String> alramJoin = Lists.newArrayList(item.getCrpNm(), item.getCrpCd(), item.getRptNm(), item.getRcpNo());
			if (StringUtils.equalsIgnoreCase(StringUtils.join(alramJoin, "|"), lastAlramItem)) {
				break;
			}
			news.add(item);
		}
		return news;
	}

	/**
	 * Save last alram item.
	 *
	 * @param now the now
	 * @param news the news
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void saveLastAlramItem(LocalDate now, List<ListItem> news) throws IOException {
		if (CollectionUtils.isEmpty(news)) {
			return;
		}

		final ListItem item = news.get(0);
		final List<String> alramJoin = Lists.newArrayList(item.getCrpNm(), item.getCrpCd(), item.getRptNm(), item.getRcpNo());

		final File saveLastAlram = new File(String.format(SAVE_FILE_FORMATT, now.getYear(), now.getMonthValue(), now.getDayOfMonth()));
		Files.write(StringUtils.join(alramJoin, "|"), saveLastAlram, Charset.defaultCharset());
	}

	/**
	 * Gets the last alram item.
	 *
	 * @param now the now
	 * @return the last alram item
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String readLastAlramItem(LocalDate now) throws IOException {
		final File readLastAlram = new File(String.format(SAVE_FILE_FORMATT, now.getYear(), now.getMonthValue(), now.getDayOfMonth()));
		return (readLastAlram.exists()) ? Files.readFirstLine(readLastAlram, Charset.defaultCharset()) : StringUtils.EMPTY;
	}
}