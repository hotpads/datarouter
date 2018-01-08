/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.model.field;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.text.StringEscapeUtils;

import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.bytes.ByteTool;
import io.datarouter.util.bytes.StringByteTool;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.lang.ObjectTool;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.varint.VarInt;

public class FieldTool{

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


	/**************************** bytes ******************/

	/*
	 * the trailingSeparatorAfterEndingString is for backwards compatibility with some early tables
	 * that appended a trailing 0 to the byte[] even though it wasn't necessary
	 */
	public static byte[] getConcatenatedValueBytes(Collection<Field<?>> fields, boolean allowNulls,
			boolean terminateIntermediateString, boolean terminateFinalString){
		int totalFields = CollectionTool.size(fields);
		int numNonNullFields = FieldTool.countNonNullLeadingFields(fields);
		if(numNonNullFields == 0){
			return null;
		}
		byte[][] fieldArraysWithSeparators = new byte[CollectionTool.size(fields)][];
		int fieldIdx = -1;
		for(Field<?> field : IterableTool.nullSafe(fields)){
			++fieldIdx;
			boolean finalField = fieldIdx == totalFields - 1;
			boolean lastNonNullField = fieldIdx == numNonNullFields - 1;
			if(!allowNulls && field.getValue() == null){
				throw new IllegalArgumentException("field:" + field.getKey().getName() + " cannot be null in");
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
		return ByteTool.concatenate(fieldArraysWithSeparators);
	}

	/*
	 * should combine this with getConcatenatedValueBytes
	 */
	public static byte[] getBytesForNonNullFieldsWithNoTrailingSeparator(List<Field<?>> fields){
		int numNonNullFields = countNonNullLeadingFields(fields);
		byte[][] fieldArraysWithSeparators = new byte[numNonNullFields][];
		int fieldIdx = -1;
		for(Field<?> field : IterableTool.nullSafe(fields)){
			++fieldIdx;
			if(fieldIdx == numNonNullFields - 1){//last field
				fieldArraysWithSeparators[fieldIdx] = field.getBytes();
				break;
			}
			fieldArraysWithSeparators[fieldIdx] = field.getBytesWithSeparator();
		}
		return ByteTool.concatenate(fieldArraysWithSeparators);
	}

	/**
	 * @param includePrefix usually refers to the "key." prefix before a PK
	 * @param skipNullValues important to include nulls in PK's, but usually skip them in normal fields
	 */
	public static byte[] getSerializedKeyValues(Collection<Field<?>> fields, boolean includePrefix,
			boolean skipNullValues){
		if(CollectionTool.isEmpty(fields)){
			return new byte[0];
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for(Field<?> field : IterableTool.nullSafe(fields)){
			//prep the values
			byte[] keyBytes;
			if(includePrefix){
				keyBytes = StringByteTool.getUtf8Bytes(field.getPrefixedName());
			}else{
				keyBytes = field.getKey().getColumnNameBytes();
			}
			VarInt keyLength = new VarInt(ArrayTool.length(keyBytes));
			byte[] valueBytes = field.getBytes();
			VarInt valueLength = new VarInt(ArrayTool.length(valueBytes));
			//abort if value is 0 bytes
			if(valueBytes == null && skipNullValues){
				continue;
			}
			try{
				//write out the bytes
				baos.write(keyLength.getBytes());
				baos.write(keyBytes);
				baos.write(valueLength.getBytes());
				baos.write(valueBytes);
			}catch(Exception e){
				throw new RuntimeException("Failed writing " + field, e);
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
		for(Field<?> field : IterableTool.nullSafe(fields)){
			if(appended > 0){
				sb.append(", ");
			}
			sb.append(field.getKey().getColumnName());
			++appended;
		}
	}

	public static void appendCsvColumnNamesWithPrefix(String prefix, StringBuilder sb, Iterable<Field<?>> fields){
		int appended = 0;
		for(Field<?> field : IterableTool.nullSafe(fields)){
			if(appended > 0){
				sb.append(", ");
			}
			sb.append(prefix + "." + field.getKey().getColumnName());
			++appended;
		}
	}

	public static List<String> getCsvColumnNamesList(Iterable<Field<?>> fields,
			Map<String,String> columnNameToCsvHeaderName){
		List<String> csvRow = new LinkedList<>();
		for(Field<?> field : IterableTool.nullSafe(fields)){
			String columnName = field.getKey().getColumnName();
			if(columnNameToCsvHeaderName != null
					&& columnNameToCsvHeaderName.containsKey(field.getKey().getColumnName())){
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
		for(Field<?> field : IterableTool.nullSafe(fields)){
			if(appended > 0){
				sb.append(",");
			}
			String valueString = field.getValueString();
			if(StringTool.isEmpty(valueString)){
				continue;
			}
			sb.append(StringEscapeUtils.escapeCsv(valueString));
			++appended;
		}
	}


	public static List<String> getCsvValuesList(Iterable<Field<?>> fields,
			Map<String,Function<Object,String>> columnNameToCsvValueFunctor, boolean emptyForNullValue){
		List<String> csvRow = new LinkedList<>();
		for(Field<?> field : IterableTool.nullSafe(fields)){
			String value = ObjectTool.nullSafeToString(field.getValue());
			if(columnNameToCsvValueFunctor != null
					&& columnNameToCsvValueFunctor.containsKey(field.getKey().getColumnName())){
				value = columnNameToCsvValueFunctor.get(field.getKey().getColumnName()).apply(field.getValue());
			}
			if(value == null && emptyForNullValue){
				value = "";
			}
			csvRow.add(value);
		}
		return csvRow;
	}

	public static List<String> getFieldNames(List<Field<?>> fields){
		List<String> fieldNames = new LinkedList<>();
		for(Field<?> field : IterableTool.nullSafe(fields)){
			fieldNames.add(field.getKey().getName());
		}
		return fieldNames;
	}

	public static List<?> getFieldValues(List<Field<?>> fields){
		List<Object> fieldValues = new LinkedList<>();
		for(Field<?> field : IterableTool.nullSafe(fields)){
			fieldValues.add(field.getValue());
		}
		return fieldValues;
	}

	public static Object getFieldValue(List<Field<?>> fields, String fieldName){
		for(Field<?> field : IterableTool.nullSafe(fields)){
			if(field.getKey().getName().equals(fieldName)){
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


	/************************** reflection ***********************/

	public static Object getNestedFieldSet(Object object, Field<?> field){
		if(StringTool.isEmpty(field.getPrefix())){
			return object;//no prefixes
		}
		List<String> fieldNames = ListTool.createArrayList(field.getPrefix().split("\\."));
		Object current = object;
		for(int i = 0; i < fieldNames.size(); ++i){//return the FieldSet, not the actual Integer (or whatever) field
			current = ReflectionTool.get(fieldNames.get(i), current);
		}
		return current;
	}

	public static java.lang.reflect.Field getReflectionFieldForField(Object object, Field<?> field){
		List<String> fieldNames = new LinkedList<>();
		if(StringTool.notEmpty(field.getPrefix())){
			fieldNames = ListTool.createArrayList(field.getPrefix().split("\\."));
		}
		fieldNames.add(field.getKey().getName());
		return ReflectionTool.getNestedField(object, fieldNames);
	}

}