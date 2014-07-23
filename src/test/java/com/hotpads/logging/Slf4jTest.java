package com.hotpads.logging;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jTest{
	private static final Logger logger = LoggerFactory.getLogger(Slf4jTest.class);

	@Test
	public void test() {
		logger.trace("trace");
		logger.debug("debug");
		logger.info("info");
		logger.warn("warn");
		logger.error("error");
	}
}
