package com.sojw.ahnchangho.batch.job.dividenstock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.sojw.ahnchangho.batch.BatchConstants;
import com.sojw.ahnchangho.core.util.EnvUtils;
import com.sojw.ahnchangho.core.util.ResourceLoaderUtil;

@Component
public class DividendRecordSearchTasklet implements Tasklet {
	private static final Logger LOG = LoggerFactory.getLogger(DividendRecordSearchTasklet.class);

	private static final String DIVIDEND_RECORD_URL = "http://finance.daum.net/invest/dividend.daum?shcode=";
	private static final String OUTPUT_FILE_NAME = "dividend_record.txt";

	@Autowired
	private ResourceLoaderUtil resourceLoaderUtil;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		List<String> stockCodeList = readStockCode();

		Map<String, String> rankMap = Maps.newHashMap();
		stockCodeList.parallelStream().forEach(stockCode -> {
			Document document = null;
			try {
				document = Jsoup.connect(DIVIDEND_RECORD_URL + stockCode).timeout(30000).get();
				if (document == null) {
					return;
				}
			} catch (IOException e) {
				LOG.error("", e);
				return;
			}

			Element topTr = document.select("tr.tr_top").first();
			if (topTr == null) {
				LOG.debug("empty record : {}", stockCode);
				return;
			}

			Element name = topTr.select("td").first();
			if (name == null) {
				LOG.debug("empty record : {}", stockCode);
				return;
			}

			StringBuilder stringBuilder = new StringBuilder();
			Element div = document.select("div.past_info").first();
			for (Element item : div.select("dl.cont_past")) {
				Element year = item.select(".tit_g").first();
				Element pay = item.select(".cont_g").first();

				stringBuilder.append(year.text() + " : " + pay.text().replaceAll(",", "") + "  ");
			}

			rankMap.put(stockCode, name.text() + "," + stringBuilder.toString());
			LOG.debug(name.text() + "," + stringBuilder.toString());
		});

		if (MapUtils.isNotEmpty(rankMap)) {
			saveRecordData(rankMap);
		}
		return RepeatStatus.FINISHED;
	}

	private List<String> readStockCode() throws IOException {
		List<String> stockCodeList = Lists.newArrayList();
		try (BufferedReader reader = new BufferedReader(resourceLoaderUtil.getReader(BatchConstants.STOCK_CODE_PATH))) {
			String thisLine;
			while ((thisLine = reader.readLine()) != null) {
				stockCodeList.add(thisLine);
			}
		} catch (Exception e) {
			LOG.error("", e);
		}

		LOG.info("stock code count : {}", stockCodeList.size());
		return stockCodeList;
	}

	private void saveRecordData(Map<String, String> rankMap) throws FileNotFoundException, IOException {
		BufferedWriter bufferedWriter = Files.newWriter(new File(EnvUtils.getValue("outputPath") + OUTPUT_FILE_NAME), Charset.defaultCharset());
		bufferedWriter.write("종목코드 , 회사명, 배당기록 \r\n");
		for (Map.Entry<String, String> item : rankMap.entrySet()) {
			bufferedWriter.write("" + item.getKey() + ", " + item.getValue() + "\r\n");
		}
		bufferedWriter.close();
	}
}