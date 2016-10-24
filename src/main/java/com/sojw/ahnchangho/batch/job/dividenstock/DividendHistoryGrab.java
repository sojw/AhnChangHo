/**
 * 
 */
package com.sojw.ahnchangho.batch.job.dividenstock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * @author Naver
 *
 */
public class DividendHistoryGrab {
	public static void main(String[] args) throws IOException {
		DividendHistoryGrab obj = new DividendHistoryGrab();
		obj.grab();
	}

	/**
	 * Grab.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void grab() throws IOException {
		List<String> stockCodeList = readFile();
		//		List<String> stockCodeList = Lists.newArrayList("004800");

		Map<String, String> rank = Maps.newHashMap();
		stockCodeList.parallelStream().forEach(stockCode -> {
			Document document = null;
			try {
				document = Jsoup.connect("http://kind.krx.co.kr/disclosureinfo/dividendinfo.do").timeout(10000).header("Content-Type", "application/x-www-form-urlencoded").data(
					"method", "searchDividendInfoSub").data("orderMode", "0").data("orderStat", "A").data("searchCodeType", "char").data("repIsuSrtCd", "A" + stockCode).data(
						"forward", "dividendinfo_sub").data("selYear", "2015").data("selYearCnt", "3").data("searchCorpName", stockCode).data("searchCorpNameTmp",
							stockCode).post();
			} catch (IOException e) {
				System.out.println(e);
			}

			if (document != null) {

				StringBuilder sb = new StringBuilder();
				for (Element item : document.select("tbody > tr")) {

					Elements tds = item.getElementsByTag("td");
					if (tds.size() < 4) {
						System.out.println("stockCode = " + stockCode);
						System.out.println(tds.html());
						continue;
					}

					String name = "";
					String year = "";
					String pay = "";
					if (item.hasClass("first")) {
						name = item.getElementsByTag("td").get(0).text();
						year = item.getElementsByTag("td").get(1).text();
						pay = item.getElementsByTag("td").get(4).text();
					} else {
						year = item.getElementsByTag("td").get(0).text();
						pay = item.getElementsByTag("td").get(3).text();
					}

					//					System.out.println("item : " + item);
					//					System.out.println("year : " + year);
					//					System.out.println("pay : " + pay);

					//					System.out.println(year + ":" + pay + "^");
					if (StringUtils.isNotEmpty(name)) {
						sb.append(name + ",");
					}
					sb.append(year + ":" + pay + "^");
				}

				System.out.println(sb.toString());
				rank.put(stockCode, sb.toString());
			}
		});

		saveData(rank);
	}

	private List<String> readFile() throws IOException {
		List<String> stockCodeList = Files.readLines(new File("C:\\Users\\Naver\\Desktop\\stock_code_ver2.txt"), Charset.defaultCharset());
		System.out.println("read stock code count : " + stockCodeList.size());
		return stockCodeList;
	}

	private void saveData(Map<String, String> rank) throws FileNotFoundException, IOException {
		BufferedWriter bufferedWriter = Files.newWriter(new File("C:\\Users\\Naver\\Desktop\\dividend_history.txt"), Charset.defaultCharset());
		for (Map.Entry<String, String> item : rank.entrySet()) {
			bufferedWriter.write("" + item.getKey() + ", " + item.getValue() + "\r\n");
		}
		bufferedWriter.close();
	}
}