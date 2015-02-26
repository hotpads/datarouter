package com.hotpads.util.core.concurrent;

import com.hotpads.datarouter.util.core.ArrayTool;




public class ThreadTool {
	
	public static String getStackTraceAsString(){
		return getPartialStackTraceAsString(Integer.MAX_VALUE);
	}
	
	public static String getPartialStackTraceAsString(int numLines){
		StringBuilder stackTrace = new StringBuilder();
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		numLines = Math.min(numLines, stack.length);
		for(int n = 0; n < numLines; n++) { 
			stackTrace.append("\tat " +stack[n].toString()+"\n");
		}	
		return stackTrace.toString();
	}
	
	public static void sleep(long ms){
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static StackTraceElement[] getStackTrace(){
		return (new Exception()).getStackTrace();
	}
	
	public static StackTraceElement getTopStackTraceElement(){
		return ArrayTool.getFirst(getStackTrace());
	}
}
