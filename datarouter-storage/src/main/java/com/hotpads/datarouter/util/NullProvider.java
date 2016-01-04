package com.hotpads.datarouter.util;

import com.google.inject.Provider;

public class NullProvider<T> implements Provider<T>{
	
	@Override
	public T get(){
		return null;
	}
	
	public static <T> Class<Provider<T>> create(Class<T> cls){
		return (Class<Provider<T>>)new NullProvider<T>().getClass();
	}
	
}