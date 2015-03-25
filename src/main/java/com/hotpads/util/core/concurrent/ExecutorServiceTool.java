package com.hotpads.util.core.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceTool {
	
	public static void shutdown(ExecutorService exec){
		exec.shutdown();
		awaitTerminationForever(exec);
	}

	public static void awaitTerminationForever(ExecutorService exec){
		try {
			exec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
}
