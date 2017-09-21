package com.sojw.ahnchangho;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sojw.ahnchangho.batch.config.SpringBatchConfig;
import com.sojw.ahnchangho.core.config.RootContextConfig;

/**
 * @author sojw
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RootContextConfig.class, SpringBatchConfig.class})
public abstract class TestApplicationContext {
	protected static final Logger LOG = LoggerFactory.getLogger(TestApplicationContext.class);

	@BeforeClass
	public static void init() throws Exception {
		// test/resources 아래에 log4j.xml 넣어둠
		//Log4jConfigurer.initLogging("classpath:log4j.xml");
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {

	}
}