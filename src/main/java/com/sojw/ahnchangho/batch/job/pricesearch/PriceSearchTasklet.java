package com.sojw.ahnchangho.batch.job.pricesearch;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

@Component
public class PriceSearchTasklet implements Tasklet {
	private static final Logger LOG = LoggerFactory.getLogger(PriceSearchTasklet.class);

	private static final String SEARCH_URL = "http://www.gap.com/resources/productSearch/v1/baby%20boys?&isFacetsEnabled=true&globalShippingCountryCode=&globalShippingCurrencyCode=&locale=en_US&pageId=";
//		private static final String SEARCH_URL = "http://www.gap.com/resources/productSearch/v1/men?&isFacetsEnabled=true&globalShippingCountryCode=&globalShippingCurrencyCode=&locale=en_US&pageId=";
	//	private static final String SEARCH_URL = "http://www.gap.com/resources/productSearch/v1/baby%20boys?&isFacetsEnabled=true&globalShippingCountryCode=&globalShippingCurrencyCode=&locale=en_US&pageId=";
	private static final String SAVE_HTML_FORMATT = "C:\\Users\\Naver\\Desktop\\price_search\\price_search_%s-%s-%s.html";
	private static final String LINE_API_KEY = "dIX1BrjfvA48pXpSTgCMk1ieaBz9mQOTqCUOCOqndw8";

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private MustacheFactory mustacheFactory;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		LocalDate now = LocalDate.now();
		final String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		LOG.debug("date = {}", date);

		List<PriceSearchResult.ProductCategoryFacetedSearch.ProductCategory.ChildProduct> totalNews = Lists.newArrayList();

		for (int i = 0;; i++) {
			try {
				final PriceSearchResult result = restTemplate.getForObject(SEARCH_URL + String.valueOf(i), PriceSearchResult.class);
				LOG.debug("result = {}", result);
				if (result.productCategoryFacetedSearch.productCategory.childProducts == null) {
					break;
				}

				final List<PriceSearchResult.ProductCategoryFacetedSearch.ProductCategory.ChildProduct> news = getNews(result);
				LOG.debug("news = {}", news);
				totalNews.addAll(news);

				final String msg = msg(news);
				LOG.debug("msg = {}", msg);
				//			sendLineMsq(msg);
			} catch (Exception e) {
				LOG.error("", e);
			}
		}

		Map<String, List<PriceSearchResult.ProductCategoryFacetedSearch.ProductCategory.ChildProduct>> scopes = Maps.newHashMap();
		scopes.put("list", totalNews);

		final String html = render(scopes);
		File saveHtml = new File(String.format(SAVE_HTML_FORMATT, now.getYear(), now.getMonthValue(), now.getDayOfMonth()));
		Files.write(html, saveHtml, Charset.defaultCharset());

		return RepeatStatus.FINISHED;
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
		Mustache mustache = mustacheFactory.compile("mustache/price_search_view.mustache");
		mustache.execute(new PrintWriter(writer), view).flush();
		return writer.toString();
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
		LOG.debug("result = {}", result);
	}

	/**
	 * Msg.
	 *
	 * @param news the news
	 * @return the string
	 */
	private String msg(List<PriceSearchResult.ProductCategoryFacetedSearch.ProductCategory.ChildProduct> news) {
		StringBuilder sb = new StringBuilder();
		for (PriceSearchResult.ProductCategoryFacetedSearch.ProductCategory.ChildProduct item : news) {
			sb.append(item.name + " | " + item.mupMessage + " , http://www.gap.com/browse/product.do?vid=1&pid=" + item.businessCatalogItemId + "\r\n\r\n");
		}
		return sb.toString();
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

	/**
	 * Gets the news.
	 *
	 * @param result the result
	 * @param lastAlramItem the last alram item
	 * @return the news
	 */
	private List<PriceSearchResult.ProductCategoryFacetedSearch.ProductCategory.ChildProduct> getNews(final PriceSearchResult result) {
		if (result.productCategoryFacetedSearch.productCategory.childProducts == null) {
			return Collections.emptyList();
		}

		List<PriceSearchResult.ProductCategoryFacetedSearch.ProductCategory.ChildProduct> news = Lists.newArrayList();
		for (PriceSearchResult.ProductCategoryFacetedSearch.ProductCategory.ChildProduct item : result.productCategoryFacetedSearch.productCategory.childProducts) {
			final Double regularPrice = NumberUtils.toDouble(item.price.currentMaxPrice);
			if (regularPrice < 22) {
				news.add(item);
			}

			if (!Strings.isNullOrEmpty(item.mupMessage)) {
				String mupMessage = item.mupMessage;
				final Double salePrice = NumberUtils.toDouble(mupMessage.replaceAll("Now", "").replaceAll("\\$", "").replaceAll(" ", ""));

				LOG.debug("salePrice === {}" , salePrice);
				news.add(item);
				
//				if ((((regularPrice - salePrice) * 100) / regularPrice) > 50) {
//					news.add(item);
//				} else if (StringUtils.startsWith(item.mupMessage, "Now ") && salePrice < 16) {
//					news.add(item);
//				}
			}

		}
		return news;
	}
}