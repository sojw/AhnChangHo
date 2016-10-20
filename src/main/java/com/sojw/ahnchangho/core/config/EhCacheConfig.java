/* 
* EhCacheConfig.java 2016. 8. 11. 
* 
* Copyright 2016 NAVER Corp. All rights Reserved. 
* NAVER PROPRIETARY/CONFIDENTIAL. Use is subject to license terms. 
*/
package com.sojw.ahnchangho.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.data.redis.serializer.SerializationException;

import com.sojw.ahnchangho.core.type.EhCacheKeys;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

@Configuration
@EnableCaching
public class EhCacheConfig extends CachingConfigurerSupport {
	private static final Logger LOG = LoggerFactory.getLogger(EhCacheConfig.class);
	//	public static final String DEFAULT_CACHE_NAME = "plug_ehcache";

	/**
	 * Cache manager.
	 *
	 * @return the cache manager
	 */
	@Bean
	public EhCacheCacheManager ehCacheCacheManager() {
		return new EhCacheCacheManager(ehCache());
	}

	/**
	 * Eh cache cache manager.
	 *
	 * @return the net.sf.ehcache. cache manager
	 */
	@Bean
	public CacheManager ehCache() {
		CacheConfiguration defaultCacheConfiguration = new CacheConfiguration() {
			{
				setMemoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU.toString());
				setMaxEntriesLocalHeap(10000);
				setEternal(Boolean.FALSE);
				addPersistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.NONE));
				setTimeToIdleSeconds(0);
				setTimeToLiveSeconds(60);
			}
		};

		//		CacheConfiguration cacheConfiguration = new CacheConfiguration() {
		//			{
		//				setName(DEFAULT_CACHE_NAME);
		//				setMemoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU.toString());
		//				setMaxEntriesLocalHeap(1000);
		//				setEternal(Boolean.FALSE);
		//				addPersistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.NONE));
		//				setTimeToIdleSeconds(0);
		//				setTimeToLiveSeconds(600);
		//			}
		//		};

		net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
		config.setDefaultCacheConfiguration(defaultCacheConfiguration);
		//		config.addCache(cacheConfiguration);
		customizeRegistration(config);

		return CacheManager.newInstance(config);
	}

	/**
	 * Register cache.
	 *
	 * @param config the config
	 */
	public void customizeRegistration(net.sf.ehcache.config.Configuration config) {
		if (config == null) {
			return;
		}

		for (EhCacheKeys.Configuration each : EhCacheKeys.Configuration.values()) {
			CacheConfiguration cacheConfiguration = new CacheConfiguration() {
				{
					setName(each.getKey());
					setMaxEntriesLocalHeap(each.getMaxEntriesLocalHeap());
					setTimeToIdleSeconds(0);
					setTimeToLiveSeconds(each.getTimeToLiveSeconds());
					setEternal(Boolean.FALSE);
					setMemoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU.toString());
					addPersistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.NONE));
				}
			};
			config.addCache(cacheConfiguration);
		}
	}

	/**
	 * The Class RelaxedCacheErrorHandler.
	 */
	private static class RelaxedCacheErrorHandler extends SimpleCacheErrorHandler {
		@Override
		public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
			//			LOG.warn("Error getting from cache." + exception);
			if (exception instanceof SerializationException || exception instanceof SerializationFailedException) {
				LOG.error("Error getting from cache." + exception);
			} else {
				LOG.error("", exception);
				throw exception;
			}
		}
	}

	@Override
	public CacheErrorHandler errorHandler() {
		return new RelaxedCacheErrorHandler();
	}
}