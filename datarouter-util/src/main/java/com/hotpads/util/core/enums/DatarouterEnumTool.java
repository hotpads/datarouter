package com.hotpads.util.core.enums;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.hotpads.datarouter.util.Validated;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public class DatarouterEnumTool{

	/*************************** comparator that compares the persistent values ***********/

    public static class IntegerEnumComparator<T extends IntegerEnum<T>> implements Comparator<T>{
	    @Override
		public int compare(T a, T b){
	    	if(DrObjectTool.bothNull(a, b)){ return 0; }
	    	if(DrObjectTool.isOneNullButNotTheOther(a, b)){ return a==null?-1:1; }
	        return DrComparableTool.nullFirstCompareTo(
	        		a.getPersistentInteger(), b.getPersistentInteger());
	    }
	}

    public static <T extends IntegerEnum<T>> int compareIntegerEnums(T a, T b){
    	if(DrObjectTool.bothNull(a, b)){
    		return 0;
    	}
    	if(DrObjectTool.isOneNullButNotTheOther(a, b)){
    		return a==null?-1:1;
    	}
        return DrComparableTool.nullFirstCompareTo(a.getPersistentInteger(), b.getPersistentInteger());
    }

    public static <T extends StringEnum<T>> int compareStringEnums(T a, T b){
    	if(DrObjectTool.bothNull(a, b)){
    		return 0;
    	}
    	if(DrObjectTool.isOneNullButNotTheOther(a, b)){
    		return a==null?-1:1;
    	}
        return DrComparableTool.nullFirstCompareTo(a.getPersistentString(), b.getPersistentString());
    }


    /********************** methods **************************************/

	public static <T extends IntegerEnum<T>> T getEnumFromInteger(T[] values, Integer value, T defaultEnum){
		if(value==null){
			return defaultEnum;
		}
		for(T type:values){
			if(type.getPersistentInteger().equals(value)){
				return type;
			}
		}
		return defaultEnum;
	}

	public static <T extends StringEnum<T>> T getEnumFromString(T[] values, String value, T defaultEnum,
			boolean caseSensitive){
		if(value==null){
			return defaultEnum;
		}
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
		List<String> strings = new ArrayList<>();
		for(E stringEnum : DrCollectionTool.nullSafe(enums)){
			strings.add(stringEnum.getPersistentString());
		}
		return strings;
	}

	public static <E extends StringEnum<E>> List<E> fromPersistentStrings(E enumInstance,
			Collection<String> persistentStrings){
		List<E> enums = new ArrayList<>();
		for(String persistentString : DrCollectionTool.nullSafe(persistentStrings)){
			enums.add(enumInstance.fromPersistentString(persistentString));
		}
		return enums;
	}

	public static <E extends StringEnum<E>> Validated<List<E>> uniqueListFromCsvNames(E[] values, String csvNames,
			boolean defaultAll){
		Set<E> result = new LinkedHashSet<>();
		Validated<List<E>> validated = new Validated<>();

		if(DrStringTool.notEmpty(csvNames)){
			String[] types = csvNames.split("[,\\s]+");
			for(String name : types){
				if(DrStringTool.isEmpty(name)){
					continue;
				}
				E type = getEnumFromString(values, name, null, false);
				if(type == null){
					validated.addError(name);
				}else{
					result.add(type);
				}
			}
		}
		if(result.isEmpty()){
			if(defaultAll){
				for(E e : values){
					result.add(e);
				}
			}else{
				validated.addError("No value found");
			}
		}
		List<E> listResult = new ArrayList<>();
		listResult.addAll(result);
		validated.set(listResult);
		return validated;
	}
}
