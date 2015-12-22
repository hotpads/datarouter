package com.hotpads.util.core.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceTool {
	
	public static void shutdown(ExecutorService exec){
		exec.shutdown();
		awaitTerminationForever(exec);
	}

	public static void awaitTerminationForever(ExecutorService exec){
		awaitTermination(exec, Long.MAX_VALUE, TimeUnit.DAYS);
	}
	
	public static void awaitTermination(ExecutorService exec, long timeout, TimeUnit unit){
		try{
			exec.awaitTermination(timeout, unit);
		}catch(InterruptedException e){
			throw new RuntimeException(e);
		}
	}
	
}
