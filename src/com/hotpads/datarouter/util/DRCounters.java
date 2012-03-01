package com.hotpads.datarouter.util;

import com.hotpads.profile.count.collection.Counters;

public class DRCounters{
	
	public static final String
		PREFIX = "DataRouter";

	public static Long inc(String key) {
		return inc(key, 1L);
	}
	
	public static void incKeyClientTable(String key, String clientName, String tableName) {
		inc(key);
		inc(key+" "+clientName);
		inc(key+" "+clientName+" "+tableName);
	}
	
	public static void incPrefixClientNode(String key, String clientName, String nodeName) {
		inc(key);
		inc(key+" "+clientName);
		inc(key+" "+clientName+" "+nodeName);
	}

	public static Long inc(String key, long delta) {
		return Counters.inc(PREFIX+" "+key, delta);
	}
	
}
