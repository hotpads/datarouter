package com.hotpads.datarouter.storage.field;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.java.ReflectionTool;

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
			if(appended > 0){ sb.append(", "); }
			sb.append(field.getName());
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
	
	
	/************************** reflection ***********************/
	
	public static java.lang.reflect.Field getReflectionFieldForField(Databean<?> sampleDatabean, Field<?> field){
		try{
			if(field.getPrefix()!=null){
				java.lang.reflect.Field parentField = sampleDatabean.getClass().getDeclaredField(field.getPrefix());
				parentField.setAccessible(true);
				if(parentField.get(sampleDatabean)==null){
					parentField.set(sampleDatabean, ReflectionTool.create(parentField.getType()));
				}
				java.lang.reflect.Field childField = parentField.getType().getDeclaredField(field.getName());
				childField.setAccessible(true);
				return childField;
			}else{
				java.lang.reflect.Field fld = sampleDatabean.getClass().getDeclaredField(field.getName());
				fld.setAccessible(true);
				return fld;
			}
		}catch(Exception e){
			throw new RuntimeException(e);
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

	public static List<String> getSqlNameValuePairsEscaped(Collection<Field<?>> fields){
		List<String> sql = ListTool.createLinkedList();
		for(Field<?> field : IterableTool.nullSafe(fields)){
			sql.add(field.getSqlNameValuePairEscaped());
		}
		return sql;
	}

	public static String getSqlNameValuePairsEscapedConjunction(Collection<Field<?>> fields){
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
	
	public static void appendSqlUpdateClauses(StringBuilder sb, Iterable<Field<?>> fields){
		int appended = 0;
		for(Field<?> field : IterableTool.nullSafe(fields)){
			if(appended > 0){ sb.append(","); }
			sb.append(field.getName()+"=?");
			++appended;
		}
	}
	

}
