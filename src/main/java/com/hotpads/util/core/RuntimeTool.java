package com.hotpads.util.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.junit.Test;


public class RuntimeTool {

	public static void pause(long milliseconds){
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
		}
	}
		
	public static String runNative(String... command) throws InterruptedException,
			IOException {
		Runtime runtime = Runtime.getRuntime();
		Process process;
		if(command.length == 1){
			process = runtime.exec(command[0]);
		}else{
			process = runtime.exec(command);
		}
		InputStream in = process.getInputStream();
		BufferedReader bufr = new BufferedReader(new InputStreamReader(in));
		StringBuilder out = new StringBuilder();
		String line;
		int numLines = 0;
		while ((line = bufr.readLine()) != null) {
			out.append((numLines++>0?"\n":"") + line);
		}
		process.waitFor();
		return out.toString();
	}
	
	public static String runLinuxNative(String... command) 
	throws InterruptedException, IOException {
		if (isLinux())
			return runNative(command);
		return null;
	}

	public static String runWindowsNative(String command)
			throws InterruptedException, IOException {
		if (isWindows())
			return runNative(command);
		return null;
	}

	public static boolean isWindows() {
		return isOS("windows");
	}

	public static boolean isLinux() {
		return isOS("linux");
	}

	public static boolean isOS(String os) {
		String realOs = System.getProperty("os.name");
		return realOs != null
				&& realOs.toLowerCase().contains(os.toLowerCase());
	}

	/*
	 * http://forum.java.sun.com/thread.jspa?threadID=5226856&messageID=9923847
	 */
	public static boolean is64BitVM() {
		String bits = System.getProperty("sun.arch.data.model", "?");
		if (bits.equals("64")) {
			return true;
		}
		if (bits.equals("?")) {
			// probably sun.arch.data.model isn't available
			// maybe not a Sun JVM?
			// try with the vm.name property
			return System.getProperty("java.vm.name").toLowerCase().indexOf(
					"64") >= 0;
		}
		// probably 32bit
		return false;
	}

	public static short getBytesPerPointer() {
		if (is64BitVM()) {
			return 8;
		}
		return 4;
	}

	public static int getNumProcessors(){
		return Runtime.getRuntime().availableProcessors();
	}

	public static long getTotalMemory(){
		return Runtime.getRuntime().totalMemory();
	}

	public static int getTotalMemoryMBytes(){
		Double memory = RuntimeTool.getTotalMemory()/1024/1024/1.5;
		return memory.intValue();
	}
	
	public static long getFreeMemory(){
		return Runtime.getRuntime().freeMemory();
	}
	
	public static long getMaxMemory(){
		return Runtime.getRuntime().maxMemory();
	}
	
	public static class Tests {
		@Test public void testIsLinux(){
			if(isLinux()) Assert.assertFalse(isWindows());
			if(isWindows()) Assert.assertFalse(isLinux());
			Assert.assertTrue("Can't test on "+System.getProperty("os.name"), 
					isLinux()||isWindows());
		}
		@Test public void testRunLinuxNative() throws Exception{
			if(!isLinux()) return;
			Assert.assertEquals("ding", runLinuxNative("echo ding"));
			Assert.assertEquals("ding", runLinuxNative("echo","ding"));
		}
	}
	
}
