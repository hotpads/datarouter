package com.hotpads.util.core.lang;

import com.hotpads.datarouter.util.core.DrClassTool;

public class StackTraceElementTool{

	public static String getPackageName(StackTraceElement stackTraceElement){
		return DrClassTool.getPackageFromCanonicalName(stackTraceElement.getClassName());
	}

	public static String getSimpleClassName(StackTraceElement stackTraceElement){
		return DrClassTool.getSimpleNameFromCanonicalName(stackTraceElement.getClassName());
	}

}
