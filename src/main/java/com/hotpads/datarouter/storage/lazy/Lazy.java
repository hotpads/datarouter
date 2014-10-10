package com.hotpads.datarouter.storage.lazy;

public abstract class Lazy<R> {
	
	private R value;
	
	protected abstract R load();
	
	public synchronized R get(){
		if(value == null){
			value = load();
		}
		return value;
	}

}
