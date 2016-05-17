package com.hotpads.datarouter.util.core;

public class DrRuntimeTool {

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

}
