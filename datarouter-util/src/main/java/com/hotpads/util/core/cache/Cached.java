package com.hotpads.util.core.cache;

import java.util.concurrent.TimeUnit;

//TODO rename to TimeCached?
public abstract class Cached<T> extends BaseCached<T>{

	protected volatile long cacheForMs;

	public Cached(long cacheFor, TimeUnit timeUnit){
		this.cacheForMs = timeUnit.toMillis(cacheFor);
	}

	@Override
	protected boolean isExpired(){
		return System.currentTimeMillis() - cachedAtMs > cacheForMs;
	}

}
