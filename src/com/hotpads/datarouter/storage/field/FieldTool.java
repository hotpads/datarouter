package com.hotpads.datarouter.storage.field;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.util.List;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

public class FieldTool{

	public static List<Field<?>> createList(Field<?>... fields){
		return ListTool.createArrayList(fields);
	}
	
	public static int countNonNullLeadingFields(Iterable<Field<?>> fields){
		int count = 0;
		for(Field<?> field : IterableTool.nullSafe(fields)){
			if(field.getValue() != null){
				++count;
			}else{
				break;
			}
		}
		return count;
	}
	
	public static void appendCsvNames(StringBuilder sb, Iterable<Field<?>> fields){
		int appended = 0;
		for(Field<?> field : IterableTool.nullSafe(fields)){
			if(appended > 0){ sb.append(","); }
			sb.append(field.getName());
			++appended;
		}
	}
	
	public static void appendSqlUpdateClauses(StringBuilder sb, Iterable<Field<?>> fields){
		int appended = 0;
		for(Field<?> field : IterableTool.nullSafe(fields)){
			if(appended > 0){ sb.append(","); }
			sb.append(field.getName()+"=?");
			++appended;
		}
	}

	public static List<String> getFieldNames(List<Field<?>> fields){
		List<String> fieldNames = ListTool.createLinkedList();
		for(Field<?> field : IterableTool.nullSafe(fields)){
			fieldNames.add(field.getName());
		}
		return fieldNames;
	}

	public static List<?> getFieldValues(List<Field<?>> fields){
		List<Object> fieldValues = ListTool.createLinkedList();
		for(Field<?> field : IterableTool.nullSafe(fields)){
			fieldValues.add(field.getValue());
		}
		return fieldValues;
	}
	
	public static Object getFieldValue(List<Field<?>> fields, String fieldName){
		for(Field<?> field : IterableTool.nullSafe(fields)){
			if(field.getName().equals(fieldName)){
				return field.getValue();
			}
		}
		return null;
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
	public static FieldSet fieldSetFromSqlUsingBeanUtils(
			Class<? extends FieldSet> cls, List<Field<?>> fields, Object sqlObject){
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
	
	public static <D extends FieldSet> D fieldSetFromSqlUsingReflection(
			Class<D> cls, List<Field<?>> fields, Object sqlObject){
		D targetFieldSet = null;
		try{
			Object[] cols = (Object[])sqlObject;
			//use getDeclaredConstructor to access non-public constructors
			Constructor<D> constructor = cls.getDeclaredConstructor();
			constructor.setAccessible(true);
			targetFieldSet = constructor.newInstance();
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
	
	public static <D extends FieldSet> D fieldSetFromJdbcResultSetUsingReflection(
			Class<D> cls, List<Field<?>> fields, ResultSet rs){
		D targetFieldSet = null;
		try{
			//use getDeclaredConstructor to access non-public constructors
			Constructor<D> constructor = cls.getDeclaredConstructor();
			constructor.setAccessible(true);
			targetFieldSet = constructor.newInstance();
			int counter = 0;
			for(Field<?> field : fields){
				field.fromJdbcResultSetUsingReflection(targetFieldSet, rs);
				++counter;
			}
		}catch(Exception e){
			throw new DataAccessException(e.getClass().getSimpleName()+" on "+cls.getName());
		}
		return targetFieldSet;
	}

}
