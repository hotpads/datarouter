package com.hotpads.util.core;


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
	
	public static boolean sameClass(Class<?> a, Class<?> b){
		if(a==null && b==null){ return true; }
		if(a==null && b!=null){ return false; }
		if(a!=null && b==null){ return false; }
		return a.equals(b);
	}
	
	public static String getSimpleName(Class<?> cls){
		return getSimpleNameFromCanonicalName(cls.getCanonicalName());
	}
	
	public static String getSimpleNameFromCanonicalName(String canonicalClassName){
		return StringTool.getStringAfterLastOccurrence('.', canonicalClassName);
	}
	
	public static String getPackage(Class<?> cls){
		return getPackageFromCanonicalName(cls.getCanonicalName());
	}
	
	public static String getPackageFromCanonicalName(String canonicalClassName){
		return StringTool.getStringBeforeLastOccurrence('.', canonicalClassName);
	}
	
	public static void main(String[] args) {
		System.out.println("a");
	}
}
