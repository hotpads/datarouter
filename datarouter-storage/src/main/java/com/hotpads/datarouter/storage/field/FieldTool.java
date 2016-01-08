package com.hotpads.datarouter.storage.field;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;

import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.Functor;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.core.number.VarLong;

public class FieldTool{

	/**
	 * @deprecated use Arrays.asList
	 */
	@Deprecated
	public static List<Field<?>> createList(Field<?>... fields){
		return DrListTool.createArrayList(fields);
	}


	public static int countNonNullLeadingFields(Iterable<Field<?>> fields){
		int count = 0;
		for(Field<?> field : DrIterableTool.nullSafe(fields)){
			if(field.getValue() != null){
				++count;
			}else{
				break;
			}
		}
		return count;
	}


	/**************************** bytes ******************/

	/*
	 * the trailingSeparatorAfterEndingString is for backwards compatibility with some early tables
	 * that appended a trailing 0 to the byte[] even though it wasn't necessary
	 */
	public static byte[] getConcatenatedValueBytes(Collection<Field<?>> fields, boolean allowNulls,
			boolean terminateIntermediateString, boolean terminateFinalString){
		int totalFields = fields.size();
		int numNonNullFields = FieldTool.countNonNullLeadingFields(fields);
		if(numNonNullFields==0){
			return null;
		}
		byte[][] fieldArraysWithSeparators = new byte[DrCollectionTool.size(fields)][];
		int fieldIdx=-1;
		for(Field<?> field : DrIterableTool.nullSafe(fields)){
			++fieldIdx;
			boolean finalField = fieldIdx == totalFields - 1;
			boolean lastNonNullField = fieldIdx == numNonNullFields - 1;
			if(!allowNulls && field.getValue()==null){
				throw new IllegalArgumentException("field:"+field.getKey().getName()+" cannot be null in");
			}
			if(finalField){
				if(terminateFinalString){
					fieldArraysWithSeparators[fieldIdx] = field.getBytesWithSeparator();
				}else{
					fieldArraysWithSeparators[fieldIdx] = field.getBytes();
				}
			}else if(lastNonNullField){
				if(terminateIntermediateString){
					fieldArraysWithSeparators[fieldIdx] = field.getBytesWithSeparator();
				}else{
					fieldArraysWithSeparators[fieldIdx] = field.getBytes();
				}
			}else{
				fieldArraysWithSeparators[fieldIdx] = field.getBytesWithSeparator();
			}
			if(lastNonNullField){
				break;
			}
		}
		return DrByteTool.concatenate(fieldArraysWithSeparators);
	}

	/*
	 * should combine this with getConcatenatedValueBytes
	 */
	public static byte[] getBytesForNonNullFieldsWithNoTrailingSeparator(List<Field<?>> fields){
		int numNonNullFields = countNonNullLeadingFields(fields);
		byte[][] fieldArraysWithSeparators = new byte[numNonNullFields][];
		int fieldIdx=-1;
		for(Field<?> field : DrIterableTool.nullSafe(fields)){
			++fieldIdx;
			if(fieldIdx == numNonNullFields - 1){//last field
				fieldArraysWithSeparators[fieldIdx] = field.getBytes();
				break;
			}
			fieldArraysWithSeparators[fieldIdx] = field.getBytesWithSeparator();
		}
		return DrByteTool.concatenate(fieldArraysWithSeparators);
	}

	/**
	 * @param includePrefix usually refers to the "key." prefix before a PK
	 * @param skipNullValues important to include nulls in PK's, but usually skip them in normal fields
	 */
	public static byte[] getSerializedKeyValues(Collection<Field<?>> fields, boolean includePrefix,
			boolean skipNullValues){
		if(DrCollectionTool.isEmpty(fields)){
			return new byte[0];
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for(Field<?> field : DrIterableTool.nullSafe(fields)){
			//prep the values
			byte[] keyBytes;
			if(includePrefix){
				keyBytes = StringByteTool.getUtf8Bytes(field.getPrefixedName());
			}else{
				keyBytes = field.getKey().getColumnNameBytes();
			}
			VarLong keyLength = new VarLong(DrArrayTool.length(keyBytes));
			byte[] valueBytes = field.getBytes();
			VarLong valueLength = new VarLong(DrArrayTool.length(valueBytes));
			//abort if value is 0 bytes
			if(DrArrayTool.isEmpty(valueBytes) && skipNullValues){
				continue;
			}
			try{
				//write out the bytes
				baos.write(keyLength.getBytes());
				baos.write(keyBytes);
				baos.write(valueLength.getBytes());
				baos.write(valueBytes);
			}catch(IOException e){
				throw new RuntimeException("a ByteArrayOutputStream threw an IOException..."
						+ " not sure how that could happen");
			}
		}
		return baos.toByteArray();
	}


	/************************* csv ***************************/

	public static String getCsvColumnNames(Iterable<Field<?>> fields){
		StringBuilder sb = new StringBuilder();
		appendCsvColumnNames(sb, fields);
		return sb.toString();
	}

	public static void appendCsvColumnNames(StringBuilder sb, Iterable<Field<?>> fields){
		int appended = 0;
		for(Field<?> field : DrIterableTool.nullSafe(fields)){
			if(appended > 0){
				sb.append(", ");
			}
			sb.append(field.getKey().getColumnName());
			++appended;
		}
	}

	public static void appendCsvColumnNamesWithPrefix(String prefix, StringBuilder sb, Iterable<Field<?>> fields){
		int appended = 0;
		for(Field<?> field : DrIterableTool.nullSafe(fields)){
			if(appended > 0){
				sb.append(", ");
			}
			sb.append(prefix + "." + field.getKey().getColumnName());
			++appended;
		}
	}

	public static List<String> getCsvColumnNamesList(Iterable<Field<?>> fields,
			Map<String, String> columnNameToCsvHeaderName) {
		List<String> csvRow = new LinkedList<>();
		for (Field<?> field : DrIterableTool.nullSafe(fields)) {
			String columnName = field.getKey().getColumnName();
			if (columnNameToCsvHeaderName != null
					&& columnNameToCsvHeaderName.containsKey(field.getKey().getColumnName())) {
				columnName = columnNameToCsvHeaderName.get(field.getKey().getColumnName());
			}
			csvRow.add(columnName);
		}
		return csvRow;
	}

	public static String getCsvValues(Iterable<Field<?>> fields){
		StringBuilder sb = new StringBuilder();
		appendCsvValues(sb, fields);
		return sb.toString();
	}

	private static void appendCsvValues(StringBuilder sb, Iterable<Field<?>> fields){
		int appended = 0;
		for(Field<?> field : DrIterableTool.nullSafe(fields)){
			if(appended > 0){
				sb.append(",");
			}
			String valueString = field.getValueString();
			if(DrStringTool.isEmpty(valueString)){
				continue;
			}
			sb.append(StringEscapeUtils.escapeCsv(valueString));
			++appended;
		}
	}


	public static List<String> getCsvValuesList(Iterable<Field<?>> fields,
			Map<String, Functor<String, Object>> columnNameToCsvValueFunctor, boolean emptyForNullValue) {
		List<String> csvRow = new LinkedList<>();
		for (Field<?> field : DrIterableTool.nullSafe(fields)) {
			String value = DrObjectTool.nullSafeToString(field.getValue());
			if (columnNameToCsvValueFunctor != null
					&& columnNameToCsvValueFunctor.containsKey(field.getKey().getColumnName())) {
				value = columnNameToCsvValueFunctor.get(field.getKey().getColumnName()).invoke(field.getValue());
			}
			if (value == null && emptyForNullValue) {
				value = "";
			}
			csvRow.add(value);
		}
		return csvRow;
	}

	public static List<String> getFieldNames(List<Field<?>> fields){
		List<String> fieldNames = new LinkedList<>();
		for(Field<?> field : DrIterableTool.nullSafe(fields)){
			fieldNames.add(field.getKey().getName());
		}
		return fieldNames;
	}

	public static List<?> getFieldValues(List<Field<?>> fields){
		List<Object> fieldValues = new LinkedList<>();
		for(Field<?> field : DrIterableTool.nullSafe(fields)){
			fieldValues.add(field.getValue());
		}
		return fieldValues;
	}

	public static Object getFieldValue(List<Field<?>> fields, String fieldName){
		for(Field<?> field : DrIterableTool.nullSafe(fields)){
			if(field.getKey().getName().equals(fieldName)){
				return field.getValue();
			}
		}
		return null;
	}

	//prepend a new prefix to an existing prefix
	public static List<Field<?>> prependPrefixes(String prefixPrefix, List<Field<?>> fields){
		for(Field<?> field : DrIterableTool.nullSafe(fields)){
			if(DrStringTool.isEmpty(field.getPrefix())){
				field.setPrefix(prefixPrefix);
			}else{
				field.setPrefix(prefixPrefix + "." + field.getPrefix());
			}
		}
		return fields;
	}


	/************************** reflection ***********************/

	public static Object getNestedFieldSet(Object object, Field<?> field){
		List<String> fieldNames = new LinkedList<>();
		if(DrStringTool.notEmpty(field.getPrefix())){
			fieldNames = DrListTool.createArrayList(field.getPrefix().split("\\."));
		}
		fieldNames.add(field.getKey().getName());
		if(fieldNames.size()==1){
			return object;//no prefixes
		}
		Object current = object;
		for(int i=0; i < fieldNames.size()-1; ++i){//return the FieldSet, not the actual Integer (or whatever) field
			current = ReflectionTool.get(fieldNames.get(i), current);
		}
		return current;
	}

	public static java.lang.reflect.Field getReflectionFieldForField(Object object, Field<?> field){
		List<String> fieldNames = new LinkedList<>();
		if(DrStringTool.notEmpty(field.getPrefix())){
			fieldNames = DrListTool.createArrayList(field.getPrefix().split("\\."));
		}
		fieldNames.add(field.getKey().getName());
		return ReflectionTool.getNestedField(object, fieldNames);
	}

}
