package com.hotpads.handler.exception;

import com.hotpads.datarouter.profile.counter.Counters;

public class ExceptionCounters{

	public static final String PREFIX = "Exception";

	public static void inc(String key){
		inc(key, 1L);
	}

	public static void inc(String key, long delta){
		Counters.inc(PREFIX + " " + key, delta);
	}

}
