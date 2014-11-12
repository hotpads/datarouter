package com.hotpads.datarouter.util;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CallsiteRecorder{
	public static final Logger logger = LoggerFactory.getLogger(CallsiteRecorder.class);
	

	public static void record(String callsite){
		logger.warn(callsite);
	}
	
}
