package com.hotpads.util.core;

import java.util.concurrent.Callable;

public class CallableTool{

	public static <T> T callUnchecked(Callable<T> callable){
		try{
			return callable.call();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
}
