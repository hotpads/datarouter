package io.datarouter.util.cached;

import java.util.concurrent.TimeUnit;

//TODO rename to TimeCached? and move the cachedAtMs field from BaseCached to here.
public abstract class Cached<T> extends BaseCached<T>{

	protected volatile long cacheForMs;

	public Cached(long cacheFor, TimeUnit timeUnit){
		this.cacheForMs = timeUnit.toMillis(cacheFor);
	}

	@Override
	protected boolean isExpired(){
		return System.currentTimeMillis() - cachedAtMs > cacheForMs;
	}

	public void expire(){
		synchronized(this){
			cachedAtMs = 0L;
			value = null;
		}
	}
}
