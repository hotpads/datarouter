package io.datarouter.util.lang;

public class StackTraceElementTool{

	public static String getPackageName(StackTraceElement stackTraceElement){
		return ClassTool.getPackageFromCanonicalName(stackTraceElement.getClassName());
	}

	public static String getSimpleClassName(StackTraceElement stackTraceElement){
		return ClassTool.getSimpleNameFromCanonicalName(stackTraceElement.getClassName());
	}

}
