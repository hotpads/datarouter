package com.hotpads.datarouter.util.core;

import java.util.Objects;

public class DrObjectTool{

	public static <T> boolean notEquals(T first, T second){
		return !Objects.equals(first, second);
	}

	public static boolean isOneNullButNotTheOther(Object first, Object second){
		return first == null ^ second == null;
	}

	public static boolean bothNull(Object first, Object second){
		return first == null && second == null;
	}

	public static <T> T nullSafe(T object, T returnIfNull){
		return object != null ? object : returnIfNull;
	}

	public static String nullSafeToString(Object object){
		return Objects.toString(object, null);
	}

	public static boolean anyNull(Object... objects){
		for(Object o : objects){
			if(o == null){
				return true;
			}
		}
		return false;
	}

}
