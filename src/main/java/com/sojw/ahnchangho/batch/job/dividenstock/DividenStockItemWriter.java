package com.sojw.ahnchangho.batch.job.dividenstock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.sojw.ahnchangho.core.model.CompanyInfo;
import com.sojw.ahnchangho.core.util.ResourceLoaderUtil;

@Component
public class DividenStockItemWriter implements ItemWriter<Map<String, Double>> {
	private static final Logger LOG = LoggerFactory.getLogger(DividenStockItemWriter.class);
	private static final String[] DIVIDEN_RECORD_HEADER_MAPPING = {"종목코드", "회사명", "배당기록"};

	@Autowired
	private ResourceLoaderUtil resourceLoaderUtil;

	@Override
	public void write(List<? extends Map<String, Double>> items) throws Exception {
		if (CollectionUtils.isEmpty(items)) {
			LOG.warn("empty list.");
			return;
		}

		LOG.info("Writer start.");
		List<CompanyInfo> companyInfo = dividendRecord();
		Map<String, String> dividenRecordMap = companyInfo.stream().collect(Collectors.toMap(CompanyInfo::getStockCode, CompanyInfo::getDividenRecord));

		final Map<String, Double> sortedMap = items.get(0);

		Date now = new Date();
		BufferedWriter bufferedWriter = Files.newWriter(new File("C:\\Users\\Naver\\Desktop\\dividend_rank_" + now.getYear() + "-" + now.getMonth() + "-" + now.getDay() + ".txt"),
			Charset.defaultCharset());
		bufferedWriter.write("회사명 | 배당수익율 | 배당기록 \r\n\r\n");
		for (Map.Entry<String, Double> item : sortedMap.entrySet()) {

			bufferedWriter.write(item.getKey() + " | " + item.getValue() + " % | "
				+ dividenRecordMap.getOrDefault(StringUtils.substringBetween(item.getKey(), "(", ")"), StringUtils.EMPTY) + "\r\n\r\n");
		}
		bufferedWriter.close();

		LOG.info("Writer done.");
	}

	public List<CompanyInfo> dividendRecord() throws IOException {
		List<CompanyInfo> companyInfoList = Lists.newArrayList();
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(DIVIDEN_RECORD_HEADER_MAPPING);
		try (Reader reader = resourceLoaderUtil.getReader("classpath:dividend_record.csv")) {
			CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());
			for (CSVRecord record : parser) {
				String stockCode;
				String name;
				String dividenRecord;
				try {
					stockCode = record.get("종목코드");
					name = record.get("회사명");
					dividenRecord = record.get("배당기록");
				} catch (Exception e) {
					LOG.info("", e);
					continue;
				}

				//				LOG.debug("종목코드 = {}, 회사명 = {}", stockCode, name);
				companyInfoList.add(new CompanyInfo(name, stockCode, dividenRecord));
			}
		} catch (Exception e) {
			LOG.error("", e);
		}
		return companyInfoList;
	}
}