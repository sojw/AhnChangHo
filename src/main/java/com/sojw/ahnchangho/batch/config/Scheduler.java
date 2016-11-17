package com.sojw.ahnchangho.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Scheduler {
	private static final Logger LOG = LoggerFactory.getLogger(Scheduler.class);

	@Scheduled(cron = "* * * * * *")
	public void cronTest1() {
		LOG.info("매초 호출이 됩니다 ");
	}

	@Scheduled(cron = "0 * * * * *")
	public void cronTest2() {
		LOG.info("매분 호출이 됩니다 ");
	}

	@Scheduled(cron = "0 30-59 15-17 * * *")
	public void cronTest3() {
		LOG.info("오후 3시부터 매분 호출이 됩니다 ");
	}

	@Scheduled(cron = "0 51 17 * * *")
	public void cronTest4() {
		LOG.info("오후 05:51:00에 호출이 됩니다 ");
	}
}