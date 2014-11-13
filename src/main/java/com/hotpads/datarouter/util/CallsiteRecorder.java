package com.hotpads.datarouter.util;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.profile.callsite.LineOfCode;

@Singleton
public class CallsiteRecorder{
	public static final Logger logger = LoggerFactory.getLogger(CallsiteRecorder.class);

	
	public static void record(String nodeName, String datarouterMethodName, LineOfCode callsite, int numItems, 
			long durationNs){
		long durationUs = durationNs / 1000;
		String message = nodeName
				+ " " + datarouterMethodName
				+ " " + callsite.getPersistentString()
				+ " " + numItems
				+ " " + durationUs;
		logger.trace(message);
	}
	
}
