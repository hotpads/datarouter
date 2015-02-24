package com.hotpads.util.core;


public class ObjectTool {
	
	public static <T> T checkNotNull(T t){
		if(t==null){ throw new IllegalArgumentException("cannot be null"); }
		return t;
	}
	
	public static <T> boolean equals(T a, T b){
		if(bothNull(a,b)){ return true; }
		if(isOneNullButNotTheOther(a,b)){ return false; }
		return a.equals(b);
	}
	
	public static <T> boolean notEquals(T a, T b){
		return ! equals(a, b);
	}
	
	public static boolean isOneNullButNotTheOther(Object a, Object b){
		if(a==null && b!=null){
			return true;
		}
		if(a!=null && b==null){
			return true;
		}
		return false;
	}
	
	public static boolean bothNull(Object a, Object b){
		return a==null && b==null;
	}

	public static int numNulls(Object... objs){
		int numNulls = 0;
		for(int i=0; i < objs.length; ++i){
			if(objs[i]==null){
				++numNulls;
			}
		}
		return numNulls;
	}
	
	public static <T> T nullSafe(T t, T returnIfNull){
		return t != null ? t : returnIfNull;
	}
	
	public static boolean nullSafeEquals(Object a, Object b){
		if(a==null && b==null){
			return true;
		}else if(a==null || b==null){
			return false;
		}else{
			return a.equals(b);
		}
	}
	
	
	public static boolean nullSafeNotEquals(Object a, Object b){
		return ! nullSafeEquals(a, b);
	}
	
	public static boolean nullSafeEqualsTrimStringNullifyEmptyString(String a, String b){
		if("".equals(a)){ a = null; }
		if("".equals(b)){ b = null; }
		
		if(a==null && b==null){
			return true;
		}else if(a==null || b==null){
			return false;
		}else{
			return a.trim().equals(b.trim());
		}
	}

	public static String nullSafeToString(Object object) {
		return nullSafeToString(object, null);
	}

	public static String nullSafeToString(Object object, String valueIfNull) {
		if(object == null){
			return valueIfNull;
		}
		return object.toString();
	}

	public static boolean anyNonNull(Object... objects){
		for(Object o : objects){
			if(o != null){
				return true;
			}
		}
		return false;
	}

	public static boolean anyNull(Object... objects){
		for(Object o : objects){
			if(o == null){
				return true;
			}
		}
		return false;
	}

	public static boolean allNull(Object... objects){
		return ! anyNonNull(objects);
	}

	public static boolean noNulls(Object ... objects){
		return ! anyNull(objects);
	}

}
