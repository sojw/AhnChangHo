/**
 * 
 */
package com.sojw.ahnchangho;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
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

import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * @author Naver
 *
 */
public class DividendGrab {

	public static void main(String[] args) throws IOException {
		DividendGrab obj = new DividendGrab();
		obj.grab();
	}

	public void grab() throws IOException {
		List<String> stockCodeList = readFile();
		//		stockCodeList = Lists.newArrayList("004800", "093190", "010660");

		Map<String, Double> rank = Maps.newHashMap();
		stockCodeList.forEach(stockCode -> {
			Document document = null;
			try {
				document = Jsoup.connect("http://companyinfo.stock.naver.com/v1/company/c1010001.aspx?cmp_cd=" + stockCode).get();
			} catch (IOException e) {
				System.out.println(e);
			}

			if (document != null) {
				Elements elements = document.getElementsByClass("line-left");
				final String companyName = document.select("span.name").text();

				for (Element element : elements) {
					if (StringUtils.startsWith(element.text(), "현금배당수익률")) {
						final Double num = NumberUtils.toDouble(element.getElementsByTag("b").first().text().replaceAll("%", ""));
						if (num > 0) {
							rank.put(companyName, num);
						}
						break;
					}
				}
			}
		});

		saveData(rank);
	}

	private void saveData(Map<String, Double> rank) throws FileNotFoundException, IOException {
		BufferedWriter bufferedWriter = Files.newWriter(new File("C:\\Users\\Naver\\Desktop\\dividend_rank.txt"), Charset.defaultCharset());
		Map<String, Double> sortedMap = sortByValue(rank);
		for (Map.Entry<String, Double> item : sortedMap.entrySet()) {
			bufferedWriter.write("" + item.getKey() + ", " + item.getValue() + "\r\n");
		}
		bufferedWriter.close();
	}

	private List<String> readFile() throws IOException {
		List<String> stockCodeList = Files.readLines(new File("C:\\Users\\Naver\\Desktop\\stockcode.txt"), Charset.defaultCharset());
		System.out.println("read stock code count : " + stockCodeList.size());
		return stockCodeList;
	}

	public Map<String, Double> sortByValue(Map<String, Double> unsortedMap) {
		Map<String, Double> sortedMap = new TreeMap<String, Double>(new ValueComparator(unsortedMap));
		sortedMap.putAll(unsortedMap);
		return sortedMap;
	}

	class ValueComparator implements Comparator {
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