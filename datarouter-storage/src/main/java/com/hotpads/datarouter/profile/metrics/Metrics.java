package com.hotpads.datarouter.profile.metrics;

public interface Metrics{

	void measure(String key, long value);

	default void measure(String key, int value){
		measure(key, Long.valueOf(value));
	}

}
