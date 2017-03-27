package com.hotpads.datarouter.config.configurer.strategy;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NoneConfigStrategy implements ConfigStrategy{
	private static final Logger logger = LoggerFactory.getLogger(NoneConfigStrategy.class);

	@Override
	public void configure(String configDirectory){
		logger.warn("executing {}", getClass().getSimpleName());
	}

}
