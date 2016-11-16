package com.sojw.ahnchangho.batch.job.dividenstock;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.sojw.ahnchangho.core.model.CompanyInfo;

@Component
public class DividenStockItemProcessor implements ItemProcessor<List<CompanyInfo>, Map<String, Double>> {
	private static final Logger LOG = LoggerFactory.getLogger(DividenStockItemProcessor.class);
	private static final String COMPANY_INFO = "http://companyinfo.stock.naver.com/v1/company/c1010001.aspx?cmp_cd=";
	private static final Integer BANK_RATE = 2;

	@Override
	public Map<String, Double> process(List<CompanyInfo> stockCodeList) throws Exception {
		LOG.info("Processor start.");

		Map<String, Double> rank = Maps.newHashMap();
		stockCodeList.parallelStream().forEach(item -> {
			Document companyInfoDocument = null;
			try {
				companyInfoDocument = Jsoup.connect(COMPANY_INFO + item.getStockCode()).get();
			} catch (IOException e) {
				LOG.error("", e);
			}

			if (companyInfoDocument != null) {
				final Elements lineLeftelements = companyInfoDocument.getElementsByClass("line-left");
				//				final String companyName = document.select("span.name").text();

				for (Element element : lineLeftelements) {
					if (!StringUtils.startsWith(element.text(), "현금배당수익률")) {
						continue;
					}

					final Double num = NumberUtils.toDouble(element.getElementsByTag("b").first().text().replaceAll("%", ""));
					// 시중 금리 낮은 경우. 제외
					if (num < BANK_RATE) {
						continue;
					}

					rank.put(item.getName() + "(" + item.getStockCode() + ")", num);
					//							LOG.info("stockCode : {}, 회사: {}, 현금배당수익률 : {}", item.getStockCode(), item.getName(), num);
					break;
				}
			} else {
				LOG.warn("empty stockCode : {}", item.getStockCode());
			}

		});

		LOG.info("rank count : {}", rank.size());
		LOG.info("Processor done.");
		return sortByValue(rank);
	}

	private Map<String, Double> sortByValue(Map<String, Double> unsortedMap) {
		Map<String, Double> sortedMap = new TreeMap<String, Double>(new ValueComparator(unsortedMap));
		sortedMap.putAll(unsortedMap);
		return sortedMap;
	}

	class ValueComparator implements Comparator<Object> {
		Map<String, Double> map;

		public ValueComparator(Map<String, Double> map) {
			this.map = map;
		}

		public int compare(Object keyA, Object keyB) {
			if (map.get(keyA) >= map.get(keyB)) { //반대로 하면 오름차순 <=
				return -1;
			} else {
				return 1;
			} // returning 0 would merge keys
		}
	}
}