package com.hotpads.datarouter.storage.field;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.java.ReflectionTool;

public class FieldTool{
	static Logger logger = Logger.getLogger(FieldTool.class);

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

	public static String getCsvColumnNames(Iterable<Field<?>> fields){
		StringBuilder sb = new StringBuilder();
		appendCsvColumnNames(sb, fields);
		return sb.toString();
	}
	
	public static void appendCsvColumnNames(StringBuilder sb, Iterable<Field<?>> fields){
		int appended = 0;
		for(Field<?> field : IterableTool.nullSafe(fields)){
			if(appended > 0){ sb.append(","); }
			sb.append(field.getColumnName());
			++appended;
		}
	}
	
	public static void appendCsvColumnNamesWithPrefix(String prefix, StringBuilder sb, Iterable<Field<?>> fields){
		int appended = 0;
		for(Field<?> field : IterableTool.nullSafe(fields)){
			if(appended > 0){ sb.append(", "); }
			sb.append(prefix + "." + field.getColumnName());
			++appended;
		}
	}

	public static String getCsvValues(Iterable<Field<?>> fields){
		StringBuilder sb = new StringBuilder();
		appendCsvValues(sb, fields);
		return sb.toString();
	}
	
	public static void appendCsvValues(StringBuilder sb, Iterable<Field<?>> fields){
		int appended = 0;
		for(Field<?> field : IterableTool.nullSafe(fields)){
			if(appended > 0){ sb.append(","); }
			String valueString = field.getValueString();
			if(StringTool.isEmpty(valueString)){ continue; }
			sb.append(StringEscapeUtils.escapeCsv(valueString));
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
	
	//prepend a new prefix to an existing prefix
	public static List<Field<?>> prependPrefixes(String prefixPrefix, List<Field<?>> fields){
		for(Field<?> field : IterableTool.nullSafe(fields)){
			if(StringTool.isEmpty(field.getPrefix())){
				field.setPrefix(prefixPrefix);
			}else{
				field.setPrefix(prefixPrefix + "." + field.getPrefix());
			}
		}
		return fields;
	}
	
	public static List<Field<?>> setPrefixes(String prefix, List<Field<?>> fields){
		for(Field<?> field : IterableTool.nullSafe(fields)){
			field.setPrefix(prefix);
		}
		return fields;
	}
	
	public static List<Field<?>> cacheReflectionInfo(List<Field<?>> fields, FieldSet<?> sampleFieldSet){
		for(Field<?> field : IterableTool.nullSafe(fields)){
			field.cacheReflectionInfo(sampleFieldSet);
		}
		return fields;
	}
	
	
	/************************** reflection ***********************/
	
//	@Deprecated //use use recursive method below
//	public static java.lang.reflect.Field getReflectionFieldForField(Databean<?,?> sampleDatabean, Field<?> field){
//		try{
//			if(field.getPrefix()!=null){
//				java.lang.reflect.Field parentField = ReflectionTool.getDeclaredFieldFromHierarchy(
//						sampleDatabean.getClass(), field.getPrefix());
//				parentField.setAccessible(true);
//				if(parentField.get(sampleDatabean)==null){
//					parentField.set(sampleDatabean, ReflectionTool.create(parentField.getType()));
//				}
//				Class<?> parentFieldClass = parentField.getType();
//				java.lang.reflect.Field childField = ReflectionTool.getDeclaredFieldFromHierarchy(
//						parentFieldClass, field.getName());
//				childField.setAccessible(true);
//				return childField;
//			}else{
//				java.lang.reflect.Field fld = ReflectionTool.getDeclaredFieldFromHierarchy(
//						sampleDatabean.getClass(), field.getName());
//				fld.setAccessible(true);
//				return fld;
//			}
//		}catch(Exception e){
//			String message = "could not set field: "+sampleDatabean.getDatabeanName()+"."+field.getPrefixedName();
//			throw new RuntimeException(message, e);
//		}
//	}
	
	public static Object getNestedFieldSet(Object object, Field<?> field){
		List<String> fieldNames = ListTool.createLinkedList();
		if(StringTool.notEmpty(field.getPrefix())){
			fieldNames = ListTool.createArrayList(field.getPrefix().split("\\."));
		}
		fieldNames.add(field.getName());
		if(fieldNames.size()==1){ return object; }//no prefixes
		Object current = object;
		for(int i=0; i < fieldNames.size()-1; ++i){//return the FieldSet, not the actual Integer (or whatever) field
			current = ReflectionTool.get(fieldNames.get(i), current);
		}
		return current;
	}
	
	public static java.lang.reflect.Field getReflectionFieldForField(Object object, Field<?> field){
		List<String> fieldNames = ListTool.createLinkedList();
		if(StringTool.notEmpty(field.getPrefix())){
			fieldNames = ListTool.createArrayList(field.getPrefix().split("\\."));
		}
		fieldNames.add(field.getName());
		return ReflectionTool.getNestedField(object, fieldNames);
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
			sb.append(field.getColumnName()+"=?");
			++appended;
		}
	}
	

}
