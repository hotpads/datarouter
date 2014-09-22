package com.hotpads.handler.exception;

import com.hotpads.profile.count.collection.Counters;

public class ExceptionCounters{

	public static final String PREFIX = "Exception";

	public static Long inc(String key){
		return inc(key, 1L);
	}

	public static Long inc(String key, long delta){
		return Counters.inc(PREFIX + " " + key, delta);
	}

}
