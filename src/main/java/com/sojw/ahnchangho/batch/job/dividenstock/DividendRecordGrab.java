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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * @author Naver
 *
 */
public class DividendRecordGrab {
	public static void main(String[] args) throws IOException {
		DividendRecordGrab obj = new DividendRecordGrab();
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
				document = Jsoup.connect("http://finance.daum.net/invest/dividend.daum?value=201512&shcode=" + stockCode).timeout(30000).get();
			} catch (IOException e) {
				System.out.println(e);
			}

			if (document == null) {
				return;
			}

			StringBuilder sb = new StringBuilder();

			Element topTr = document.select("tr.tr_top").first();
			if (topTr == null) {
				System.out.println("empty record : " + stockCode);
				return;
			}

			Element name = topTr.select("td").first();
			//			System.out.println(name.text());
			if (name == null) {
				return;
			}

			Element div = document.select("div.past_info").first();
			for (Element item : div.select("dl.cont_past")) {
				Element year = item.select(".tit_g").first();
				Element pay = item.select(".cont_g").first();

				sb.append(year.text() + " : " + pay.text().replaceAll(",", "") + "  ");
				//					System.out.println(year.text() + ":" + pay.text() + "^");
			}

			System.out.println(name.text() + "," + sb.toString());
			rank.put(stockCode, name.text() + "," + sb.toString());
		});

		saveData(rank);
	}

	private List<String> readFile() throws IOException {
		List<String> stockCodeList = Files.readLines(new File("C:\\Users\\Naver\\Desktop\\stock_code_ver2.txt"), Charset.defaultCharset());
		System.out.println("read stock code count : " + stockCodeList.size());
		return stockCodeList;
	}

	private void saveData(Map<String, String> rank) throws FileNotFoundException, IOException {
		BufferedWriter bufferedWriter = Files.newWriter(new File("C:\\Users\\Naver\\Desktop\\dividend_record.txt"), Charset.defaultCharset());
		bufferedWriter.write("종목코드 , 회사명, 배당기록 \r\n");
		for (Map.Entry<String, String> item : rank.entrySet()) {
			bufferedWriter.write("" + item.getKey() + ", " + item.getValue() + "\r\n");
		}
		bufferedWriter.close();
	}
}