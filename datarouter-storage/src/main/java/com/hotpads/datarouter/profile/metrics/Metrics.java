package com.hotpads.datarouter.profile.metrics;

public interface Metrics{

	void save(String key, long value);

	default void measure(String key, int value){
		save(key, Long.valueOf(value));
	}

}
