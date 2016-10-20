package com.sojw.ahnchangho.batch.job.dividenstock;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

@Component
public class DividenStockItemReader extends AbstractPagingItemReader<String> {
	private static final Logger LOG = LoggerFactory.getLogger(DividenStockItemReader.class);

	@Override
	protected void doReadPage() {
		if (results == null) {
			results = new CopyOnWriteArrayList<String>();
		} else {
			results.clear();
		}

		try {
			results = Files.readLines(new File("C:\\Users\\Naver\\Desktop\\stockcode.txt"), Charset.defaultCharset());
		} catch (IOException e) {
			LOG.error("", e);
		}

		results = Lists.newArrayList();
	}

	@Override
	protected void doJumpToPage(int i) {

	}
}