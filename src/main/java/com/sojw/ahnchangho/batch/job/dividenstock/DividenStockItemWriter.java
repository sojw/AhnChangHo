package com.sojw.ahnchangho.batch.job.dividenstock;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.google.common.io.Files;

@Component
public class DividenStockItemWriter implements ItemWriter<Map<String, Double>> {
	private static final Logger LOG = LoggerFactory.getLogger(DividenStockItemWriter.class);

	@Override
	public void write(List<? extends Map<String, Double>> items) throws Exception {
		if (CollectionUtils.isEmpty(items)) {
			LOG.warn("empty list.");
			return;
		}

		LOG.info("Writer start.");
		final Map<String, Double> sortedMap = items.get(0);

		Date now = new Date();
		BufferedWriter bufferedWriter = Files.newWriter(new File("C:\\Users\\Naver\\Desktop\\dividend_rank_" + now.getYear() + "-" + now.getMonth() + "-" + now.getDay() + ".txt"),
			Charset.defaultCharset());
		for (Map.Entry<String, Double> item : sortedMap.entrySet()) {
			bufferedWriter.write("회사 : " + item.getKey() + " | 배당수익율: " + item.getValue() + "\r\n\r\n");
		}
		bufferedWriter.close();

		LOG.info("Writer done.");
	}
}