/* 
* EhCacheKeys.java 2016. 10. 12. 
* 
* Copyright 2016 NAVER Corp. All rights Reserved. 
* NAVER PROPRIETARY/CONFIDENTIAL. Use is subject to license terms. 
*/
package com.sojw.ahnchangho.core.type;

/**
 * The Class EhCacheKeys.
 */
public class EhCacheKeys {
	public static final String ONE_MIN = "ehCache.1min";
	public static final String FIVE_MIN = "ehCache.5min";
	public static final String TEN_MIN = "ehCache.10min";
	public static final String HALF_HOUR = "ehCache.30min";
	public static final String ONE_HOUR = "ehCache.1hour";
	public static final String HALF_DAY = "ehCache.12hour";
	public static final String ONE_DAY = "ehCache.1day";

	public enum Configuration {
		/* @formatter:off */
		ONE_MIN(EhCacheKeys.ONE_MIN, "", 60L),
		FIVE_MIN(EhCacheKeys.FIVE_MIN, "", 300L),
		TEN_MIN(EhCacheKeys.TEN_MIN, "", 600L),
		HALF_HOUR(EhCacheKeys.HALF_HOUR, "", 1800L),
		ONE_HOUR(EhCacheKeys.ONE_HOUR, "", 3600L),
		HALF_DAY(EhCacheKeys.HALF_DAY, "", 3600L * 12),
		ONE_DAY(EhCacheKeys.ONE_DAY, "", 3600L * 24);
		/* @formatter:on */

		private final String key;
		private final String desc;
		private final Long maxEntriesLocalHeap;
		private final Long timeToLiveSeconds;
		private final Long defaultMaxEntriesLocalHeap = 1000L;

		Configuration(String key, String desc, Long timeToLiveSeconds) {
			this.key = key;
			this.desc = desc;
			this.timeToLiveSeconds = timeToLiveSeconds;
			this.maxEntriesLocalHeap = defaultMaxEntriesLocalHeap;
		}

		Configuration(String key, String desc, Long timeToLiveSeconds, Long maxEntriesLocalHeap) {
			this.key = key;
			this.desc = desc;
			this.timeToLiveSeconds = timeToLiveSeconds;
			this.maxEntriesLocalHeap = maxEntriesLocalHeap;
		}

		public String getKey() {
			return key;
		}

		public String getDesc() {
			return desc;
		}

		public Long getMaxEntriesLocalHeap() {
			return maxEntriesLocalHeap;
		}

		public Long getTimeToLiveSeconds() {
			return timeToLiveSeconds;
		}

		public static Configuration find(String key) {
			for (Configuration each : Configuration.values()) {
				if (each.getKey().equals(key)) {
					return each;
				}
			}
			return null;
		}
	}
}