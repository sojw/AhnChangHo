package com.sojw.ahnchangho.batch.job.dividenstock;

import java.io.IOException;

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

import com.sojw.ahnchangho.core.model.DividenStock;

@Component
public class DividenStockItemProcessor implements ItemProcessor<String, DividenStock> {
	private static final Logger LOG = LoggerFactory.getLogger(DividenStockItemProcessor.class);

	@Override
	public DividenStock process(String stockCode) throws Exception {
		DividenStock dividenStock = null;
		Document document = null;
		try {
			document = Jsoup.connect("http://companyinfo.stock.naver.com/v1/company/c1010001.aspx?cmp_cd=" + stockCode).get();
		} catch (IOException e) {
			LOG.error("", e);
		}

		if (document != null) {
			Elements elements = document.getElementsByClass("line-left");
			final String companyName = document.select("span.name").text();

			for (Element element : elements) {
				if (StringUtils.startsWith(element.text(), "현금배당수익률")) {
					final Double num = NumberUtils.toDouble(element.getElementsByTag("b").first().text().replaceAll("%", ""));
					if (num > 0) {
						dividenStock = new DividenStock(companyName, num);
					}
					break;
				}
			}
		}

		return dividenStock;
	}
}