package com.sojw.ahnchangho.batch.job.stockalram;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.sojw.ahnchangho.batch.job.stockalram.SearchResult.ListItem;
import com.sojw.ahnchangho.core.line.LineClient;
import com.sojw.ahnchangho.core.util.FileUtils;
import com.sojw.ahnchangho.core.util.UriUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class StockalramTasklet.
 */
@Slf4j
@Component
public class StockalramTasklet implements Tasklet {
	private static final String STOCK_NOTICE_VIEW = "http://m.dart.fss.or.kr/html_mdart/MD1007.html?rcpNo=";
	//	private static final String DART_API_URL = "http://dart.fss.or.kr/api/search.json?auth=%s&page_set=50&start_dt=%s&dsp_tp=A&dsp_tp=B&dsp_tp=D&dsp_tp=F&dsp_tp=I&dsp_tp=J&page_no=3";

	private static final String DART_API_URL = "http://dart.fss.or.kr/api/search.json?auth=%s&page_set=100&start_dt=%s";
	private static final String DART_API_KEY = "62d77fc171e7f887beac19fc86b4d20df1337be3";

	private static final String SAVE_FILE_FORMATT = FileUtils.currentRelativeRootPath() + System.getProperties().getProperty("file.separator") + "stock_alram_%s-%s-%s.txt";

	private static final List<String> FIND_KEYWORD_LIST = Lists.newArrayList("유상증자결정", "공급계약체결", "단일판매ㆍ공급계약체결", "특수관계인의유상증자참여", "유상증자참여", "최대주주변경을수반하는주식양수도계약체결", "영업(잠정)실적(공정공시)",
		"연결재무제표기준영업(잠정)실적(공정공시)", "실적", "제3자배정", "배당", "자기주식취득");

	private static final List<String> EXCLUDE_KEYWORD_LIST = Lists.newArrayList("기재정정", "종속회사의주요경영사항", "결산실적공시예고", "자회사의 주요경영사항", "자회사의주요경영사항", "증권발행실적보고서", "증권발행결과");

	private static final String NEWS_DELIMITER = "|";
	private static String KOSDAQ = "K";//코스닥
	private static String KOSPI = "Y";//유가증권

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private LineClient lineClient;

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.step.tasklet.Tasklet#execute(org.springframework.batch.core.StepContribution, org.springframework.batch.core.scope.context.ChunkContext)
	 */
	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		LocalDate now = LocalDate.now();
		final String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		log.debug("date = {}", date);

		try {
			final SearchResult result = restTemplate.getForObject(String.format(DART_API_URL, DART_API_KEY, date), SearchResult.class);
			if (!StringUtils.equalsIgnoreCase(result.getErrCode(), "000")) {
				log.error("error!!! result = {}", result);
				return RepeatStatus.FINISHED;
			}

			if (CollectionUtils.isEmpty(result.getList())) {
				log.info("news is empty.", result);
				return RepeatStatus.FINISHED;
			}

			final List<ListItem> news = extractNews(result, readLastNewsItem(now));
			log.debug("news.size() = {}", news.size());

			saveLastNewsItem(now, news);
			sendNews(news);

		} catch (Exception e) {
			log.error("", e);
		}

		return RepeatStatus.FINISHED;
	}

	/**
	 * Send news.
	 *
	 * @param news the news
	 */
	private void sendNews(final List<ListItem> news) {
		for (ListItem item : news) {
			String msg = "[" + item.getCrpNm() + "] " + item.getRptNm() + " " + STOCK_NOTICE_VIEW + item.getRcpNo() + " , " + "http://m.stock.naver.com/item/main.nhn#/stocks/" + item.getCrpCd()
				+ "/discuss" + "\r\n\r\n";
			log.debug("msg = {}", msg);

			lineClient.send(msg);
		}
	}

	/**
	 * Extract news.
	 *
	 * @param result the result
	 * @param lastNewsId the last alram item
	 * @return the list
	 */
	private List<ListItem> extractNews(final SearchResult result, final String lastNewsId) {
		log.debug("last news id = {}", lastNewsId);

		List<ListItem> news = Lists.newArrayList();
		for (ListItem item : result.getList()) {
			// 새로운 뉴스가 없는 경우,
			if (StringUtils.equalsIgnoreCase(item.getRcpNo(), lastNewsId)) {
				break;
			}

			// 코스닥, 코스피 종목이 아닌 경우,
			if (!StringUtils.containsAny(item.getCrpCls(), KOSPI, KOSDAQ)) {
				continue;
			}

			// 제외 키워드가 포함 된 경우,
			if (excludeKeyword(item.getRptNm())) {
				continue;
			}

			// 알림 키워드가 포함 된 경우,
			if (findKeyword(item.getRptNm())) {
				appendNewsTitle(item);
				news.add(item);
			}
		}
		return news;
	}

	/**
	 * Append news title.
	 *
	 * @param item the item
	 */
	private void appendNewsTitle(final ListItem item) {
		if (StringUtils.contains(item.getRptNm().replaceAll(" ", ""), "유상증자")) {
			if (searchNewsContents(item.getRcpNo(), "제3자배정증자")) {
				item.setRptNm(item.getRptNm() + " - 제3자배정증자!!!!!");
			}
		}
	}

	/**
	 * Exclude keyword.
	 *
	 * @param rptNm the rpt nm
	 * @return the boolean
	 */
	private Boolean excludeKeyword(final String rptNm) {
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
	private Boolean findKeyword(final String rptNm) {
		for (String keyword : FIND_KEYWORD_LIST) {
			if (StringUtils.contains(rptNm, keyword)) {
				return Boolean.TRUE;
			}
		}

		return Boolean.FALSE;
	}

	/**
	 * Search news contents.
	 *
	 * @param rcpNo the rcp no
	 * @param findKeyword the find keyword
	 * @return the boolean
	 */
	private Boolean searchNewsContents(final String rcpNo, final String findKeyword) {
		try {
			final ResponseEntity<Map<String, String>> result = restTemplate.exchange(UriUtils.of("http://m.dart.fss.or.kr/viewer/main.st?rcpNo=" + rcpNo, Collections.emptyMap()), HttpMethod.GET, null,
				new ParameterizedTypeReference<Map<String, String>>() {});
			if (StringUtils.contains(result.getBody().get("reportBody"), findKeyword)) {
				return Boolean.TRUE;
			}

			//			final Map<String, String> result = restTemplate.getForObject("http://m.dart.fss.or.kr/viewer/main.st?rcpNo=" + rcpNo, Map.class);
			//			if (StringUtils.contains(result.get("reportBody"), findKeyword)) {
			//				return Boolean.TRUE;
			//			}
		} catch (Exception e) {
			log.error("", e);
		}

		return Boolean.FALSE;
	}

	/**
	 * Save last news item.
	 *
	 * @param now the now
	 * @param news the news
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void saveLastNewsItem(final LocalDate now, final List<ListItem> news) throws IOException {
		if (CollectionUtils.isEmpty(news)) {
			return;
		}

		final ListItem item = news.get(0);
		final List<String> alramJoin = Lists.newArrayList(item.getCrpNm(), item.getCrpCd(), item.getRptNm(), item.getRcpNo());
		final File saveLastAlram = new File(String.format(SAVE_FILE_FORMATT, now.getYear(), now.getMonthValue(), now.getDayOfMonth()));
		Files.write(StringUtils.join(alramJoin, NEWS_DELIMITER), saveLastAlram, Charset.defaultCharset());
		Files.write(item.getRcpNo(), saveLastAlram, Charset.defaultCharset());
	}

	/**
	 * Read last news item.
	 *
	 * @param now the now
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String readLastNewsItem(final LocalDate now) throws IOException {
		final File readLastAlram = new File(String.format(SAVE_FILE_FORMATT, now.getYear(), now.getMonthValue(), now.getDayOfMonth()));
		return (readLastAlram.exists()) ? Files.readFirstLine(readLastAlram, Charset.defaultCharset()) : StringUtils.EMPTY;
	}
}