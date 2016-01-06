package com.hotpads.util.core.concurrent;

public class ThreadTool {

	public static void sleep(long ms){
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e){
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}

}
