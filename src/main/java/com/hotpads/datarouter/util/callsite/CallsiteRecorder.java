package com.hotpads.datarouter.util.callsite;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.profile.callsite.LineOfCode;

@Singleton
public class CallsiteRecorder{
	private static final Logger logger = LoggerFactory.getLogger(CallsiteRecorder.class);

	
	public static void record(String nodeName, String datarouterMethodName, LineOfCode callsite, int numItems, 
			long durationNs){
		if(!logger.isTraceEnabled()){ return; }
		CallsiteRecord record = new CallsiteRecord(nodeName, datarouterMethodName, callsite.getPersistentString(),
				numItems, durationNs);
		logger.trace(record.getLogMessage());
	}
	
}
