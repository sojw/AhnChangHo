package com.sojw.ahnchangho;

import java.io.IOException;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.sojw.ahnchangho.core.util.ResourceLoaderUtil;

public class CsvTest extends TestApplicationContext {
	@Autowired
	private ResourceLoaderUtil resourceLoaderUtil;

	//CSV file header
	private static final String[] FILE_HEADER_MAPPING = {"종목코드", "회사명"};

	@Test
	public void read() throws IOException {
		//		Reader reader = resourceLoaderUtil.getReader("classpath:stock_code_raw.csv");
		//		Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(reader);
		//		for (CSVRecord record : records) {
		//			String stockCode = record.get("종목코드");
		//			String company = record.get("회사명");
		//
		//			LOG.debug("stockCode = {}, company = {}", stockCode, company);
		//		}
		//		reader.close();

		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER_MAPPING);

		try (Reader reader = resourceLoaderUtil.getReader("classpath:stock_code_raw.csv")) {
			CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());
			for (CSVRecord record : parser) {
				String stockCode = record.get("종목코드");
				String company = record.get("회사명");

				LOG.debug("종목코드 = {}, 회사명 = {}", stockCode, company);
			}
		} catch (Exception e) {
			LOG.error("", e);
		}
	}
}