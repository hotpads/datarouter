package com.hotpads.util.core.lang;

import com.hotpads.datarouter.util.core.ClassTool;

public class StackTraceElementTool{

	public static String getPackageName(StackTraceElement stackTraceElement){
		return ClassTool.getPackageFromCanonicalName(stackTraceElement.getClassName());
	}

	public static String getSimpleClassName(StackTraceElement stackTraceElement){
		return ClassTool.getSimpleNameFromCanonicalName(stackTraceElement.getClassName());
	}

}
