package com.hotpads.util.core.concurrent;

public abstract class Lazy<R> {
	
	private volatile R value;
	
	protected abstract R load();
	
	public R get(){
		if(value != null){
			return value;
		}
		synchronized (this){
			if(value != null){
				return value;
			}
			return value = load();
		}
	}

}
