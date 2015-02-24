package com.hotpads.util.core;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

public class CallableTool{

	public static <T> T callUnchecked(Callable<T> callable){
		try{
			return callable.call();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public static <T> Map<Callable<T>,T> callMultiUnchecked(Collection<? extends Callable<T>> callables){
		Map<Callable<T>,T> resultByCallable = MapTool.createHashMap();
		for(Callable<T> callable : IterableTool.nullSafe(callables)){
			resultByCallable.put(callable, callUnchecked(callable));
		}
		return resultByCallable;
	}
	
}
