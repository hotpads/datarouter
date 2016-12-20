package com.hotpads.datarouter.util.core;

public class DrRuntimeTool{

	public static boolean isWindows(){
		return isOS("windows");
	}

	public static boolean isOS(String os){
		String realOs = System.getProperty("os.name");
		return realOs != null
				&& realOs.toLowerCase().contains(os.toLowerCase());
	}

	/*
	 * http://forum.java.sun.com/thread.jspa?threadID=5226856&messageID=9923847
	 */
	public static boolean is64BitVM(){
		String bits = System.getProperty("sun.arch.data.model", "?");
		if("64".equals(bits)){
			return true;
		}
		if("?".equals(bits)){
			// probably sun.arch.data.model isn't available
			// maybe not a Sun JVM?
			// try with the vm.name property
			return System.getProperty("java.vm.name").toLowerCase().indexOf(
					"64") >= 0;
		}
		// probably 32bit
		return false;
	}

	public static short getBytesPerPointer(){
		if(is64BitVM()){
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
		Double memory = getTotalMemory() / 1024 / 1024 / 1.5;
		return memory.intValue();
	}

}
