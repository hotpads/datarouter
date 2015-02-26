package com.hotpads.util.core.cache;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.core.DrObjectTool;

public abstract class Cached<T>{
	protected static final Logger logger = LoggerFactory.getLogger(Cached.class);
	
	protected volatile long cacheForMs;
	
	protected T t;
	protected volatile long cachedAtMs = -1;
	
	
	public Cached(long cacheFor, TimeUnit timeUnit){
		this.cacheForMs = timeUnit.toMillis(cacheFor);
	}
	
	
	protected abstract T reload();
	
	
	public T get(){
		updateIfExpired();
		return t;
	}
	
	/*
	 * todo make this async in case the reload method is slow
	 */
	protected boolean updateIfExpired(){
		if(!isExpired()){ return false; }
		T original;
		synchronized(this){
			original = t;
			if(!isExpired()){ return false; }
			t = reload();
			cachedAtMs = System.currentTimeMillis();
		}
		return DrObjectTool.notEquals(original, t);
		
	}
	
	protected boolean isExpired(){
		return System.currentTimeMillis() - cachedAtMs > cacheForMs;
	}
	
	
}
