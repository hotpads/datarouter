package com.hotpads.datarouter.util.core;

import java.util.HashSet;
import java.util.Set;

public class BooleanTool {
	

	public static final Set<String> TRUE_VALUES = new HashSet<String>();
	public static final Set<String> FALSE_VALUES = new HashSet<String>();
	
	static{
		TRUE_VALUES.add("true");
		TRUE_VALUES.add("1");
		TRUE_VALUES.add("t");
		TRUE_VALUES.add("yes");
		TRUE_VALUES.add("y");
		TRUE_VALUES.add("on");

		FALSE_VALUES.add("false");
		FALSE_VALUES.add("0");
		FALSE_VALUES.add("f");
		FALSE_VALUES.add("no");
		FALSE_VALUES.add("n");
		FALSE_VALUES.add("off");
	}
	
	public static boolean isTrue(String input){
		if(input==null) return false;
		return TRUE_VALUES.contains(input.toLowerCase());
	}
	
	public static boolean isTrueOrNull(String input){
		if(input==null) return true;
		return TRUE_VALUES.contains(input.toLowerCase());
	}
	
	public static boolean isFalse(String input){
		if(input==null) return false;
		return FALSE_VALUES.contains(input.toLowerCase());
	}
	
	public static boolean isTrue(Boolean b){
		if (b == null) return false;
		return b;
	}
	
	public static boolean isFalseOrNull(Boolean b){
		if (b == null) return true;
		return ! b;
	}
	public static boolean isFalse(Boolean b){
		if (b == null) return false;
		return ! b;
	}
}
