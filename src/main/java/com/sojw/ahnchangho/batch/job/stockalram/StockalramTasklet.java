package com.sojw.ahnchangho.batch.job.stockalram;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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
import com.sojw.ahnchangho.core.util.FileUtils;

@Component
public class StockalramTasklet implements Tasklet {
	private static final String STOCK_NOTICE_VIEW = "http://m.dart.fss.or.kr/html_mdart/MD1007.html?rcpNo=";
	private static final Logger LOG = LoggerFactory.getLogger(StockalramTasklet.class);
	//	private static final String DART_API_URL = "http://dart.fss.or.kr/api/search.json?auth=%s&page_set=50&start_dt=%s&dsp_tp=A&dsp_tp=B&dsp_tp=D&dsp_tp=F&dsp_tp=I&dsp_tp=J&page_no=3";
	private static final String DART_API_URL = "http://dart.fss.or.kr/api/search.json?auth=%s&page_set=100&start_dt=%s";
	private static final String DART_API_KEY = "62d77fc171e7f887beac19fc86b4d20df1337be3";
	private static final String LINE_API_KEY = "dIX1BrjfvA48pXpSTgCMk1ieaBz9mQOTqCUOCOqndw8";
	private static final String SAVE_FILE_FORMATT = FileUtils.currentRelativeRootPath() + System.getProperties().getProperty("file.separator") + "stock_alram_%s-%s-%s.txt";
	private static final List<String> FIND_KEYWORD_LIST = Lists.newArrayList("유상증자결정", "공급계약체결", "단일판매ㆍ공급계약체결", "특수관계인의유상증자참여", "유상증자참여", "최대주주변경을수반하는주식양수도계약체결", "영업(잠정)실적(공정공시)",
		"연결재무제표기준영업(잠정)실적(공정공시)", "실적", "제3자배정", "배당", "자기주식취득");
	private static final List<String> EXCLUDE_KEYWORD_LIST = Lists.newArrayList("기재정정", "종속회사의주요경영사항", "결산실적공시예고", "자회사의 주요경영사항", "자회사의주요경영사항", "증권발행실적보고서", "증권발행결과");
	private static final String NEWS_DELIMITER = "|";

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		LocalDate now = LocalDate.now();
		final String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		LOG.debug("date = {}", date);

		try {
			final SearchResult result = restTemplate.getForObject(String.format(DART_API_URL, DART_API_KEY, date), SearchResult.class);
			if (!StringUtils.equalsIgnoreCase(result.getErrCode(), "000")) {
				LOG.error("error!!! result = {}", result);
				return RepeatStatus.FINISHED;
			}

			if (CollectionUtils.isEmpty(result.getList())) {
				LOG.info("news is empty.", result);
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
			sb.append("[" + item.getCrpNm() + "] " + item.getRptNm() + " " + STOCK_NOTICE_VIEW + item.getRcpNo() + " , " + "http://m.stock.naver.com/item/main.nhn#/stocks/" + item.getCrpCd()
				+ "/discuss" + "\r\n\r\n");
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
		LOG.debug("lastAlramItem = {}", lastAlramItem);
		String kosdex = "K";//코스닥
		String kospi = "Y";//유가증권
		List<ListItem> news = Lists.newArrayList();
		for (ListItem item : result.getList()) {
			List<String> alramJoin = Lists.newArrayList(item.getCrpNm(), item.getCrpCd(), item.getRptNm(), item.getRcpNo());
			if (StringUtils.equalsIgnoreCase(StringUtils.join(alramJoin, NEWS_DELIMITER), lastAlramItem)) {
				break;
			}

			if (!StringUtils.containsAny(item.getCrpCls(), kospi, kosdex)) {
				continue;
			}

			if (excludeKeyword(item.getRptNm())) {
				continue;
			}

			if (findKeyword(item.getRptNm())) {
				checkNoticeView(item);
				news.add(item);
			}
		}
		return news;
	}

	/**
	 * Check notice view.
	 *
	 * @param item the item
	 */
	private void checkNoticeView(ListItem item) {
		if (StringUtils.contains(item.getRptNm().replaceAll(" ", ""), "유상증자")) {
			if (searchNoticeView(item.getRcpNo(), "제3자배정증자")) {
				item.setRptNm(item.getRptNm() + " - 제3자배정증자!!!!!");
			}
		}
	}

	/**
	 * Find exclude keyword.
	 *
	 * @param rptNm the rpt nm
	 * @return the boolean
	 */
	private Boolean excludeKeyword(String rptNm) {
		for (String keyword : EXCLUDE_KEYWORD_LIST) {
			if (StringUtils.contains(rptNm, keyword)) {
				return Boolean.TRUE;
			}
		}

		return Boolean.FALSE;
	}

	/**
	 * Find keyword.
	 *
	 * @param rptNm the rpt nm
	 * @return the boolean
	 */
	private Boolean findKeyword(String rptNm) {
		for (String keyword : FIND_KEYWORD_LIST) {
			if (StringUtils.contains(rptNm, keyword)) {
				return Boolean.TRUE;
			}
		}

		return Boolean.FALSE;
	}

	/**
	 * Contents.
	 *
	 * @param rcpNo the rcp no
	 * @param findKeyword the find keyword
	 * @return the boolean
	 */
	private Boolean searchNoticeView(String rcpNo, String findKeyword) {
		try {

			final Map<String, String> result = restTemplate.getForObject("http://m.dart.fss.or.kr/viewer/main.st?rcpNo=" + rcpNo, Map.class);
			//			LOG.debug("result = {}", result);

			if (StringUtils.contains(result.get("reportBody"), findKeyword)) {
				return Boolean.TRUE;
			}
		} catch (Exception e) {
			LOG.error("", e);
		}

		return Boolean.FALSE;
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
		Files.write(StringUtils.join(alramJoin, NEWS_DELIMITER), saveLastAlram, Charset.defaultCharset());
	}

	/**
	 * Gets the last alram item.
	 *
	 * @param now the now
	 * @return the last alram item
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String readLastAlramItem(LocalDate now) throws IOException {
		LOG.debug("readLastAlramItem path = {}", String.format(SAVE_FILE_FORMATT, now.getYear(), now.getMonthValue(), now.getDayOfMonth()));
		final File readLastAlram = new File(String.format(SAVE_FILE_FORMATT, now.getYear(), now.getMonthValue(), now.getDayOfMonth()));
		return (readLastAlram.exists()) ? Files.readFirstLine(readLastAlram, Charset.defaultCharset()) : StringUtils.EMPTY;
	}
}