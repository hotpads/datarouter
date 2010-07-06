package com.hotpads.profile.count;

public interface Counter {

	public abstract long increment(String key);

	public abstract long increment(String key, long delta);

}