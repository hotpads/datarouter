package com.hotpads.notification;

import com.hotpads.datarouter.profile.counter.Counters;

public class NotificationCounters{

	public static final String PREFIX = "Notification";

	public static void inc(String key){
		inc(key, 1L);
	}

	public static void inc(String key, long delta){
		Counters.inc(PREFIX + " " + key, delta);
	}

}
