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
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
	private static final String FINANCE_INFO = "http://companyinfo.stock.naver.com/v1/company/ajax/cF1001.aspx?fin_typ=0&freq_typ=Q&cmp_cd=";

	private static final String[] DIVIDEN_RECORD_HEADER_MAPPING = {"종목코드", "회사명", "배당기록"};
	private static final String SAVE_FILE_FORMATT = "C:\\Users\\Naver\\Desktop\\dividend_rank\\dividend_rank_%s-%s-%s.txt";
	private static final String SAVE_HTML_FORMATT = "C:\\Users\\Naver\\Desktop\\dividend_rank\\dividend_rank_%s-%s-%s.html";
	private static final String DATA_ROW_FORMATT = "%s | %s / %s % | %f % | %s \r\n\r\n";

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
		final List<CompanyInfo> companyInfo = dividendRecord();
		final Map<String, String> dividenRecordMap = companyInfo.stream().collect(Collectors.toMap(CompanyInfo::getStockCode, CompanyInfo::getDividenRecord));

		final Map<String, Double> sortedMap = items.get(0);

		LocalDate now = LocalDate.now();
		File saveFile = new File(String.format(SAVE_FILE_FORMATT, now.getYear(), now.getMonthValue(), now.getDayOfMonth()));
		List<DividenView> dividenViewList = Lists.newArrayList();

		try (BufferedWriter bufferedWriter = Files.newWriter(saveFile, Charset.defaultCharset())) {

			bufferedWriter.write("회사명 | 시가 | 배당수익률 | 배당기록 \r\n\r\n");

			for (Map.Entry<String, Double> item : sortedMap.entrySet()) {

				final String stockCode = StringUtils.substringBetween(item.getKey(), "(", ")");
				final String dividenRecord = dividenRecordMap.getOrDefault(stockCode, StringUtils.EMPTY);

				// 우선주가 아닌, 이전 배당 기록이 없는 회사는 제외.
				if (Strings.isNullOrEmpty(dividenRecord)
					&& !(StringUtils.contains(item.getKey(), "우") && NumberUtils.toInt(StringUtils.right(item.getKey(), 2).replace(")", StringUtils.EMPTY)) % 2 == 1)) {
					continue;
				}

				if (StringUtils.equals(stockCode, "083420")) {
					System.out.println("");
				}

				final PriceInfo priceInfo = todayPriceInfo(stockCode);
				bufferedWriter.write(item.getKey() + " | " + priceInfo.getNowVal() + " / " + priceInfo.getRate() + "% | " + item.getValue() + " % | " + dividenRecord + "\r\n\r\n");
				//				bufferedWriter.write(String.format(DATA_ROW_FORMATT, item.getKey(), priceInfo.getNowVal(), priceInfo.getRate(), item.getValue(), dividenRecord));
				final FinanceInfo financeInfo = financeInfo(stockCode);

				Integer improveFinancePoint = improveFinancePoint(financeInfo);
				Integer improveFinanceGrade = improveFinanceGrade(improveFinancePoint);

				DividenView dividenView = new DividenView();
				dividenView.setDivedenRecord(dividenRecord);
				dividenView.setDividen(item.getValue());
				dividenView.setNowVal(priceInfo.getNowVal());
				dividenView.setRate(priceInfo.getRate());
				dividenView.setStockName(item.getKey());
				dividenView.setStockCode(stockCode);
				dividenView.setImproveFinanceGrade(improveFinanceGrade);
				dividenView.setImproveFinancePoint(improveFinancePoint);

				dividenViewList.add(dividenView);
			}
		} catch (Exception e) {
			LOG.error("", e);
		}

		Map<String, List<DividenView>> scopes = Maps.newHashMap();
		scopes.put("dividenViewList", dividenViewList);

		final String html = render(scopes);
		File saveHtml = new File(String.format(SAVE_HTML_FORMATT, now.getYear(), now.getMonthValue(), now.getDayOfMonth()));
		Files.write(html, saveHtml, Charset.defaultCharset());

		LOG.info("Writer done.");
	}

	/**
	 * Improve finance grade.
	 *
	 * @param improveFinancePoint the improve finance point
	 * @return the integer
	 */
	private Integer improveFinanceGrade(final Integer improveFinancePoint) {
		Integer improveFinanceGrade = 0;
		Range<Integer> grade5 = Range.between(-8, -5);
		Range<Integer> grade4 = Range.between(-4, -2);
		Range<Integer> grade3 = Range.between(-1, 1);
		Range<Integer> grade2 = Range.between(2, 4);
		Range<Integer> grade1 = Range.between(5, 8);

		if (grade1.contains(improveFinancePoint)) {
			improveFinanceGrade = 1;
		} else if (grade2.contains(improveFinancePoint)) {
			improveFinanceGrade = 2;
		} else if (grade3.contains(improveFinancePoint)) {
			improveFinanceGrade = 3;
		} else if (grade4.contains(improveFinancePoint)) {
			improveFinanceGrade = 4;
		} else if (grade5.contains(improveFinancePoint)) {
			improveFinanceGrade = 5;
		}
		return improveFinanceGrade;
	}

	/**
	 * Improve finance point.
	 *
	 * @param financeInfo the finance info
	 * @return the integer
	 */
	private Integer improveFinancePoint(final FinanceInfo financeInfo) {
		Integer improveFinancePoint = 0;

		switch (financeInfo.getImproveSales()) {
			case MORE_FORWARD:

				break;
			case FORWARD:
				improveFinancePoint += 1;
				break;

			case DOWNWARD:
				improveFinancePoint -= 1;
				break;
			case MORE_DOWNWARD:

				break;
		}

		switch (financeInfo.getImproveBenefit()) {
			case MORE_FORWARD:

				break;
			case FORWARD:
				improveFinancePoint += 3;
				break;

			case DOWNWARD:
				improveFinancePoint -= 3;
				break;
			case MORE_DOWNWARD:

				break;
		}

		switch (financeInfo.getImproveDebtRate()) {
			case MORE_FORWARD:
				improveFinancePoint += 2;
				break;
			case FORWARD:
				improveFinancePoint += 1;
				break;

			case DOWNWARD:
				improveFinancePoint -= 1;
				break;
			case MORE_DOWNWARD:
				improveFinancePoint -= 2;
				break;
		}

		switch (financeInfo.getImproveRetentionRate()) {
			case MORE_FORWARD:
				improveFinancePoint += 2;
				break;
			case FORWARD:
				improveFinancePoint += 1;
				break;

			case DOWNWARD:
				improveFinancePoint -= 1;
				break;
			case MORE_DOWNWARD:
				improveFinancePoint -= 2;
				break;
		}

		return improveFinancePoint;
	}

	/**
	 * Render.
	 *
	 * @param view the view
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String render(Object view) throws IOException {
		StringWriter writer = new StringWriter();
		Mustache mustache = mustacheFactory.compile("mustache/dividend_view.mustache");
		mustache.execute(new PrintWriter(writer), view).flush();
		return writer.toString();
	}

	/**
	 * Finance info.
	 *
	 * @param stockCode the stock code
	 * @return the finance info
	 */
	private FinanceInfo financeInfo(String stockCode) {
		Document companyInfoDocument = null;
		try {
			companyInfoDocument = Jsoup.connect(FINANCE_INFO + stockCode).get();
		} catch (IOException e) {
			LOG.error("", e);
		}

		final Elements salesElements = companyInfoDocument.select("table > tbody > tr:nth-child(1) > td:not(.bgE)");
		FinanceStatus improveSales = FinanceStatus.DOWNWARD;
		if (salesElements != null) {
			final Double first = NumberUtils.toDouble(salesElements.first().text().replaceAll(",", ""));
			final Double last = NumberUtils.toDouble(salesElements.last().text().replaceAll(",", ""));
			improveSales = (last >= first) ? FinanceStatus.FORWARD : FinanceStatus.DOWNWARD;
		}

		final Elements benefitElements = companyInfoDocument.select("table > tbody > tr:nth-child(2) > td:not(.bgE)");
		FinanceStatus improveBenefit = FinanceStatus.DOWNWARD;
		if (benefitElements != null) {
			final Double first = NumberUtils.toDouble(benefitElements.first().text().replaceAll(",", ""));
			final Double last = NumberUtils.toDouble(benefitElements.last().text().replaceAll(",", ""));
			improveBenefit = (last >= first) ? FinanceStatus.FORWARD : FinanceStatus.DOWNWARD;
		}

		final Elements debtRateElements = companyInfoDocument.select("table > tbody > tr:nth-child(23) > td:not(.bgE)");
		FinanceStatus improveDebtRate = FinanceStatus.DOWNWARD;
		if (debtRateElements != null) {
			final Double first = NumberUtils.toDouble(debtRateElements.first().text().replaceAll(",", ""));
			final Double last = NumberUtils.toDouble(debtRateElements.last().text().replaceAll(",", ""));
			improveDebtRate = buildFinanceStatus(last, first, 10);
		}

		final Elements retentionRateElements = companyInfoDocument.select("table > tbody > tr:nth-child(24) > td:not(.bgE)");
		FinanceStatus improveRetentionRate = FinanceStatus.DOWNWARD;
		if (retentionRateElements != null) {
			final Double first = NumberUtils.toDouble(retentionRateElements.first().text().replaceAll(",", ""));
			final Double last = NumberUtils.toDouble(retentionRateElements.last().text().replaceAll(",", ""));
			improveRetentionRate = buildFinanceStatus(first, last, 10);
		}

		return new FinanceInfo(improveSales, improveBenefit, improveDebtRate, improveRetentionRate);
	}

	private FinanceStatus buildFinanceStatus(final Double param1, final Double param2, final Integer criterion) {
		FinanceStatus improveDebtRate;
		if (param2 >= param1) {
			improveDebtRate = (((param2 - param1) * 100) / param1 > criterion) ? FinanceStatus.MORE_FORWARD : FinanceStatus.FORWARD;
		} else {
			improveDebtRate = (((param2 - param1) * 100) / param1 < -criterion) ? FinanceStatus.MORE_DOWNWARD : FinanceStatus.DOWNWARD;
		}
		return improveDebtRate;
	}

	/**
	 * Today price info.
	 *
	 * @param stockCode the stock code
	 * @return the price info
	 */
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

	/**
	 * Dividend record.
	 *
	 * @return the list
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
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

class FinanceInfo {
	private FinanceStatus improveSales;
	private FinanceStatus improveBenefit;
	private FinanceStatus improveDebtRate;
	private FinanceStatus improveRetentionRate;

	public FinanceInfo(FinanceStatus improveSales, FinanceStatus improveBenefit, FinanceStatus improveDebtRate, FinanceStatus improveRetentionRate) {
		this.improveSales = improveSales;
		this.improveBenefit = improveBenefit;
		this.improveDebtRate = improveDebtRate;
		this.improveRetentionRate = improveRetentionRate;
	}

	public FinanceStatus getImproveSales() {
		return improveSales;
	}

	public FinanceStatus getImproveBenefit() {
		return improveBenefit;
	}

	public FinanceStatus getImproveDebtRate() {
		return improveDebtRate;
	}

	public FinanceStatus getImproveRetentionRate() {
		return improveRetentionRate;
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

enum FinanceStatus {
	MORE_FORWARD, FORWARD, DOWNWARD, MORE_DOWNWARD;
}