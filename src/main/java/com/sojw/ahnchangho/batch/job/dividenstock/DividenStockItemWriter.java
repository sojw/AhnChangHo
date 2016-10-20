package com.sojw.ahnchangho.batch.job.dividenstock;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.google.common.io.Files;
import com.sojw.ahnchangho.core.model.DividenStock;

@Component
public class DividenStockItemWriter implements ItemWriter<DividenStock> {
	private static final Logger LOG = LoggerFactory.getLogger(DividenStockItemWriter.class);

	@Override
	public void write(List<? extends DividenStock> items) throws Exception {
		final Map<String, Double> sortedMap = sortByValue(items.stream().collect(Collectors.toMap(x -> x.getName(), x -> x.getDividen())));

		BufferedWriter bufferedWriter = Files.newWriter(new File("C:\\Users\\Naver\\Desktop\\dividend_rank.txt"), Charset.defaultCharset());
		for (Map.Entry<String, Double> item : sortedMap.entrySet()) {
			bufferedWriter.write("회사 : " + item.getKey() + " | 배당수익율: " + item.getValue() + "\r\n");
		}
		bufferedWriter.close();
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