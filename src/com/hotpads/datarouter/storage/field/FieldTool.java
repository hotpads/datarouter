package com.hotpads.datarouter.storage.field;

import java.lang.reflect.Constructor;
import java.util.List;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

public class FieldTool{

	public static List<String> getFieldNames(List<Field<?>> fields){
		List<String> fieldNames = ListTool.createLinkedList();
		for(Field<?> field : IterableTool.nullSafe(fields)){
			fieldNames.add(field.getName());
		}
		return fieldNames;
	}

	public static List<Comparable<?>> getFieldValues(List<Field<?>> fields){
		List<Comparable<?>> fieldValues = ListTool.createLinkedList();
		for(Field<?> field : IterableTool.nullSafe(fields)){
			fieldValues.add(field.getValue());
		}
		return fieldValues;
	}
	
	public static Comparable<?> getFieldValue(List<Field<?>> fields, String fieldName){
		for(Field<?> field : IterableTool.nullSafe(fields)){
			if(field.getName().equals(fieldName)){
				return field.getValue();
			}
		}
		return null;
	}
	
	/************************* reflection *************************/
	
	public static List<Field<?>> getFieldsUsingReflection(Class<? extends FieldSet> cls){
		try{
			//use getDeclaredConstructor to access non-public constructors
			Constructor<?> constructor = cls.getDeclaredConstructor();
			constructor.setAccessible(true);
			FieldSet targetFieldSet = (FieldSet)constructor.newInstance();
			return targetFieldSet.getFields();
		}catch(Exception e){
			throw new DataAccessException(e.getClass().getSimpleName()+" on "+cls.getName());
		}
	}
	

	/**************************** sql ******************/

	public static List<String> getSqlValuesEscaped(List<Field<?>> fields){
		List<String> sql = ListTool.createLinkedList();
		for(Field<?> field : IterableTool.nullSafe(fields)){
			sql.add(field.getSqlEscaped());
		}
		return sql;
	}

	public static List<String> getSqlNameValuePairsEscaped(List<Field<?>> fields){
		List<String> sql = ListTool.createLinkedList();
		for(Field<?> field : IterableTool.nullSafe(fields)){
			sql.add(field.getSqlNameValuePairEscaped());
		}
		return sql;
	}

	public static String getSqlNameValuePairsEscapedConjunction(List<Field<?>> fields){
		List<String> nameValuePairs = getSqlNameValuePairsEscaped(fields);
		if(CollectionTool.sizeNullSafe(nameValuePairs) < 1){ return null; }
		StringBuilder sb = new StringBuilder();
		int numAppended = 0;
		for(String nameValuePair : nameValuePairs){
			if(numAppended > 0){ sb.append(" and "); }
			sb.append(nameValuePair);
			++numAppended;
		}
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static FieldSet fieldSetFromSqlUsingBeanUtils(Class<? extends FieldSet> cls, List<Field<?>> fields, Object sqlObject){
		FieldSet targetFieldSet = null;
		try{
			Object[] cols = (Object[])sqlObject;
			targetFieldSet = cls.newInstance();
			int counter = 0;
			for(Field field : fields){
				field.setFieldUsingBeanUtils(targetFieldSet, cols[counter]);
				++counter;
			}
		}catch(Exception e){
			throw new DataAccessException(e.getClass().getSimpleName()+" on "+cls.getName());
		}
		return targetFieldSet;
	}
	
	public static FieldSet fieldSetFromSqlUsingReflection(Class<? extends FieldSet> cls, List<Field<?>> fields, Object sqlObject){
		FieldSet targetFieldSet = null;
		try{
			Object[] cols = (Object[])sqlObject;
			//use getDeclaredConstructor to access non-public constructors
			Constructor<?> constructor = cls.getDeclaredConstructor();
			constructor.setAccessible(true);
			targetFieldSet = (FieldSet)constructor.newInstance();
			int counter = 0;
			for(Field<?> field : fields){
				field.setFieldUsingReflection(targetFieldSet, cols[counter]);
				++counter;
			}
		}catch(Exception e){
			throw new DataAccessException(e.getClass().getSimpleName()+" on "+cls.getName());
		}
		return targetFieldSet;
	}

}
