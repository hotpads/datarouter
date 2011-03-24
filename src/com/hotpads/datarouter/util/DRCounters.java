package com.hotpads.datarouter.util;

import com.hotpads.profile.count.collection.Counters;

public class DRCounters{
	
	public static final String
		PREFIX = "DataRouter";

	public static Long inc(String key) {
		return inc(key, 1L);
	}

	public static Long inc(String key, long delta) {
		return Counters.inc(PREFIX+" "+key, delta);
	}
	
}
