package com.hotpads.datarouter.storage.field.enums;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ComparableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.ObjectTool;

public class DataRouterEnumTool{
	
	/*************************** comparator that compares the persistent values ***********/

    public static class IntegerEnumComparator<T extends IntegerEnum<T>> implements Comparator<T>{
	    public int compare(T a, T b){
	    	if(ObjectTool.bothNull(a, b)){ return 0; }
	    	if(ObjectTool.isOneNullButNotTheOther(a, b)){ return a==null?-1:1; }
	        return ComparableTool.nullFirstCompareTo(
	        		a.getPersistentInteger(), b.getPersistentInteger());
	    }
	}

    public static <T extends IntegerEnum<T>> int compareIntegerEnums(T a, T b){
    	if(ObjectTool.bothNull(a, b)){
    		return 0;
    	}
    	if(ObjectTool.isOneNullButNotTheOther(a, b)){
    		return a==null?-1:1;
    	}
        return ComparableTool.nullFirstCompareTo(a.getPersistentInteger(), b.getPersistentInteger());
    }

    public static <T extends StringEnum<T>> int compareStringEnums(T a, T b){
    	if(ObjectTool.bothNull(a, b)){
    		return 0;
    	}
    	if(ObjectTool.isOneNullButNotTheOther(a, b)){
    		return a==null?-1:1;
    	}
        return ComparableTool.nullFirstCompareTo(a.getPersistentString(), b.getPersistentString());
    }
    
    
    /********************** methods **************************************/

	public static <T extends IntegerEnum<T>> T getEnumFromInteger(T[] values, Integer value, T defaultEnum){
		if(value==null) return defaultEnum;
		for(T type:values){
			if(type.getPersistentInteger().equals(value)) return type;
		}
		return defaultEnum;
	}

	public static <T extends StringEnum<T>> T getEnumFromString(T[] values, String value, T defaultEnum,
			boolean caseSensitive){
		if(value==null) return defaultEnum;
		for(T type:values){
			if((caseSensitive && type.getPersistentString().equals(value)) 
					|| (!caseSensitive && type.getPersistentString().equalsIgnoreCase(value))){
				return type;
			}
		}
		return defaultEnum;
	}
	
	public static <T extends StringEnum<T>> T getEnumFromString(T[] values, String value, T defaultEnum){
		return getEnumFromString(values,value,defaultEnum,true);
	}
	

	
	/***************  multiple values ****************/
	
	public static <E extends StringEnum<E>> List<String> getPersistentStrings(Collection<E> enums){
		List<String> strings = ListTool.createArrayList();
		for(E stringEnum : CollectionTool.nullSafe(enums)){
			strings.add(stringEnum.getPersistentString());
		}
		return strings;
	}
	
	public static <E extends StringEnum<E>> List<E> fromPersistentStrings(E enumInstance, 
			Collection<String> persistentStrings){
		List<E> enums = ListTool.createArrayList();
		for(String persistentString : CollectionTool.nullSafe(persistentStrings)){
			enums.add(enumInstance.fromPersistentString(persistentString));
		}
		return enums;
	}
	
	
}
