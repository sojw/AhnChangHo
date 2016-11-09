package com.sojw.ahnchangho.batch.job.dividenstock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.sojw.ahnchangho.core.model.CompanyInfo;
import com.sojw.ahnchangho.core.util.ResourceLoaderUtil;

@Component
public class DividenStockItemWriter implements ItemWriter<Map<String, Double>> {
	private static final Logger LOG = LoggerFactory.getLogger(DividenStockItemWriter.class);
	private static final String STOCK_PRICE = "http://finance.naver.com/item/sise.nhn?code=";
	private static final String[] DIVIDEN_RECORD_HEADER_MAPPING = {"종목코드", "회사명", "배당기록"};
	private static final String SAVE_FILE_FORMATT = "C:\\Users\\Naver\\Desktop\\dividend_rank_%s-%s-%s.txt";
	private static final String SAVE_HTML_FORMATT = "C:\\Users\\Naver\\Desktop\\dividend_rank_%s-%s-%s.html";

	@Autowired
	private ResourceLoaderUtil resourceLoaderUtil;

	@Autowired
	private MustacheFactory mustacheFactory;

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

		LocalDate now = LocalDate.now();
		File saveFile = new File(String.format(SAVE_FILE_FORMATT, now.getYear(), now.getMonthValue(), now.getDayOfMonth()));

		BufferedWriter bufferedWriter = Files.newWriter(saveFile, Charset.defaultCharset());

		List<DividenView> dividenViewList = Lists.newArrayList();
		bufferedWriter.write("회사명 | 시가 | 배당수익률 | 배당기록 \r\n\r\n");
		for (Map.Entry<String, Double> item : sortedMap.entrySet()) {

			String stockCode = StringUtils.substringBetween(item.getKey(), "(", ")");
			final String dividenRecord = dividenRecordMap.getOrDefault(stockCode, StringUtils.EMPTY);
			// 우선주가 아닌, 이전 배당 기록이 없는 회사는 제외.
			if (Strings.isNullOrEmpty(dividenRecord)) {
				if (!(StringUtils.contains(item.getKey(), "우") && NumberUtils.toInt(StringUtils.left(item.getKey(), 2).replace(")", "")) % 2 == 1)) {
					continue;
				}
			}

			PriceInfo priceInfo = todayPriceInfo(stockCode);
			bufferedWriter.write(item.getKey() + " | " + priceInfo.getNowVal() + " / " + priceInfo.getRate() + "% | " + item.getValue() + " % | " + dividenRecord + "\r\n\r\n");

			DividenView dividenView = new DividenView();
			dividenView.setDivedenRecord(dividenRecord);
			dividenView.setDividen(item.getValue());
			dividenView.setNowVal(priceInfo.getNowVal());
			dividenView.setRate(priceInfo.getRate());
			dividenView.setStockName(item.getKey());
			dividenView.setStockCode(stockCode);
			dividenViewList.add(dividenView);
		}
		bufferedWriter.close();

		Map<String, List<DividenView>> scopes = Maps.newHashMap();
		scopes.put("dividenViewList", dividenViewList);

		final String html = render(scopes);
		LOG.info("html = {}", html);
		File saveHtml = new File(String.format(SAVE_HTML_FORMATT, now.getYear(), now.getMonthValue(), now.getDayOfMonth()));
		Files.write(html, saveHtml, Charset.defaultCharset());

		LOG.info("html = {}", html);
		LOG.info("Writer done.");
	}

	private String render(Object view) throws IOException {
		StringWriter writer = new StringWriter();
		Mustache mustache = mustacheFactory.compile("mustache/dividend_view.mustache");
		mustache.execute(new PrintWriter(writer), view).flush();
		return writer.toString();
	}

	private PriceInfo todayPriceInfo(String stockCode) {
		try {
			Document stockPriceDocument = Jsoup.connect(STOCK_PRICE + stockCode).get();
			Element rateElement = stockPriceDocument.getElementById("_rate");
			String rate = rateElement == null ? StringUtils.EMPTY : rateElement.text();
			Element nowValElement = stockPriceDocument.getElementById("_nowVal");
			String nowVal = nowValElement == null ? StringUtils.EMPTY : nowValElement.text();

			return new PriceInfo(rate, nowVal);
		} catch (IOException e) {
			LOG.error("", e);
		}
		return new PriceInfo();
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

class PriceInfo {
	private String rate;
	private String nowVal;

	public PriceInfo() {
		super();
	}

	public PriceInfo(String rate, String nowVal) {
		this.rate = rate;
		this.nowVal = nowVal;
	}

	public String getRate() {
		return rate;
	}

	public String getNowVal() {
		return nowVal;
	}
}