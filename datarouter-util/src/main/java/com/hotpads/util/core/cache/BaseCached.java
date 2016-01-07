package com.hotpads.util.core.cache;

import com.hotpads.datarouter.util.core.DrObjectTool;

public abstract class BaseCached<T>{

	protected T value;
	protected volatile long cachedAtMs = -1;

	protected abstract T reload();

	public T get(){
		updateIfExpired();
		return value;
	}

	/*
	 * todo make this async in case the reload method is slow
	 */
	protected boolean updateIfExpired(){
		if(!isExpired()) {
			return false;
		}
		T original;
		synchronized(this){
			original = value;
			if(!isExpired()) {
				return false;
			}
			value = reload();
			cachedAtMs = System.currentTimeMillis();
		}
		return DrObjectTool.notEquals(original, value);

	}

	protected abstract boolean isExpired();

}
