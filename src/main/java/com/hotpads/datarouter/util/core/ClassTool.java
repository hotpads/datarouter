package com.hotpads.datarouter.util.core;


public class ClassTool {

	public static boolean sameClass(Object a, Object b){
		if(a==null && b==null){ return true; }
		if(a==null && b!=null){ return false; }
		if(a!=null && b==null){ return false; }
		return a.getClass().equals(b.getClass());
	}
	
	public static boolean differentClass(Object a, Object b){
		return !sameClass(a, b);
	}
	
	public static int compareClass(Object a, Object b){
		if(a==b){ return 0; }
		if(a==null){ return -1; }
		if(b==null){ return 1; }
		return a.getClass().getName().compareTo(b.getClass().getName());
	}
	
	public static String getSimpleNameFromCanonicalName(String canonicalClassName){
		return StringTool.getStringAfterLastOccurrence('.', canonicalClassName);
	}
	
	public static String getPackageFromCanonicalName(String canonicalClassName){
		return StringTool.getStringBeforeLastOccurrence('.', canonicalClassName);
	}
	
}
