package com.hotpads.util.core.concurrent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.hotpads.datarouter.util.core.DrIterableTool;

public class CallableTool{

	public static <T> T callUnchecked(Callable<T> callable){
		try{
			return callable.call();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	public static <T> Map<Callable<T>,T> callMultiUnchecked(Collection<? extends Callable<T>> callables){
		Map<Callable<T>,T> resultByCallable = new HashMap<>();
		for(Callable<T> callable : DrIterableTool.nullSafe(callables)){
			resultByCallable.put(callable, callUnchecked(callable));
		}
		return resultByCallable;
	}

}
