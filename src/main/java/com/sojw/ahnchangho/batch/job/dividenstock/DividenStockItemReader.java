package com.sojw.ahnchangho.batch.job.dividenstock;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.sojw.ahnchangho.core.model.CompanyInfo;
import com.sojw.ahnchangho.core.util.ResourceLoaderUtil;

@Component
public class DividenStockItemReader implements ItemReader<List<CompanyInfo>> {
	private static final Logger LOG = LoggerFactory.getLogger(DividenStockItemReader.class);

	//CSV file header
	private static final String[] FILE_HEADER_MAPPING = {"종목코드", "회사명"};

	@Autowired
	private ResourceLoaderUtil resourceLoaderUtil;

	private int count;

	@Override
	public List<CompanyInfo> read() {
		if (count > 0) {
			return null;
		}

		LOG.info("Reader start.");
		try {
			count++;

			List<CompanyInfo> companyInfoList = companyInfo();
			//			List<String> stockCodeList = Files.readLines(new File("C:\\Users\\Naver\\Desktop\\stockcode.txt"), Charset.defaultCharset());
			//			List<String> stockCodeList = resourceLoaderUtil.loadFileContents("classpath:stock_code_ver2.txt");
			//			LOG.info("stock code count : {}", stockCodeList.size());

			LOG.info("Reader done.");
			return companyInfoList;
		} catch (Exception e) {
			LOG.error("", e);
			return Collections.emptyList();
		}
	}

	/**
	 * Csv.
	 *
	 * @return the list
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public List<CompanyInfo> companyInfo() throws IOException {
		List<CompanyInfo> companyInfoList = Lists.newArrayList();
		//		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER_MAPPING);
		try (Reader reader = resourceLoaderUtil.getReader("classpath:stock_code_raw.csv")) {
			CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());
			for (CSVRecord record : parser) {
				String stockCode = StringUtils.trim(record.get("종목코드"));
				String name = StringUtils.trim(record.get("회사명"));

				//				LOG.debug("종목코드 = {}, 회사명 = {}", stockCode, name);
				companyInfoList.add(new CompanyInfo(name, stockCode, ""));
			}
		} catch (Exception e) {
			LOG.error("", e);
		}
		return companyInfoList;
	}
}