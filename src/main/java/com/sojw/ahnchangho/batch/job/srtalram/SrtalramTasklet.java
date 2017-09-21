package com.sojw.ahnchangho.batch.job.srtalram;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SrtalramTasklet implements Tasklet {
	private static final String DART_API_URL = "https://etk.srail.co.kr/hpg/hra/01/selectScheduleList.do?pageId=TK0101010000&dptRsStnCd=0551&arvRsStnCd=0297&stlbTrnClsfCd=05&psgNum=2&seatAttCd=015&isRequest=Y&dptRsStnCdNm=%EC%88%98%EC%84%9C&arvRsStnCdNm=%EC%98%A4%EC%86%A1&dptDt=20170929&dptTm=200000&chtnDvCd=1&psgInfoPerPrnb1=2&psgInfoPerPrnb5=0&psgInfoPerPrnb4=0&psgInfoPerPrnb2=0&psgInfoPerPrnb3=0&locSeatAttCd1=000&rqSeatAttCd1=015&trnGpCd=109";
	private static final String LINE_API_KEY = "aBTg5imCAEOZGDZFq2DQhnUbUlGUuTFkZC8g6WbCfUl";
	private static final String FIND_TRAIN_NO = "375";
	private static final String LIMITED = "매진";

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		LocalDate now = LocalDate.now();
		final String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		log.debug("date = {}", date);

		try {
			final String result = restTemplate.getForObject(DART_API_URL, String.class);

			Document doc = Jsoup.parse(result);
			Elements elements = doc.select("tbody > tr > td.trnNo");
			for (Element element : elements) {
				if (StringUtils.equals(FIND_TRAIN_NO, element.text())) {
					//					log.debug("{}", element);
					Element firstSeat = element.parent().child(5);
					Element secondSeat = element.parent().child(6);
					StringBuilder sb = new StringBuilder();
					Boolean send = Boolean.FALSE;
					sb.append("열차 번호 : " + FIND_TRAIN_NO + ".\n");
					if (StringUtils.equals(LIMITED, firstSeat.text())) {
						sb.append("특실 매진.\n");
					} else {
						sb.append("특실 가능.\n");
						send = Boolean.TRUE;
					}

					if (StringUtils.equals(LIMITED, secondSeat.text())) {
						sb.append("일반실 매진.\n");
					} else {
						sb.append("일반실 가능.\n");
						send = Boolean.TRUE;
					}

					log.debug("sb.toString() = {}", sb.toString());

					if (send) {
						sendLineMsq(sb.toString());
					}
				}
			}
			//			sendLineMsq("");
		} catch (Exception e) {
			log.error("", e);
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
		log.debug("result = {}", result);
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
}