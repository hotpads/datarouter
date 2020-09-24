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

import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.bytes.ByteTool;
import io.datarouter.util.bytes.StringByteTool;
import io.datarouter.util.bytes.VarInt;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.string.StringTool;

public class FieldTool{

	public static int countNonNullLeadingFields(Iterable<Field<?>> fields){
		int count = 0;
		for(Field<?> field : fields){
			if(field.getValue() != null){
				++count;
			}else{
				break;
			}
		}
		return count;
	}


	/*------------------------------- bytes ---------------------------------*/

	/*
	 * the trailingSeparatorAfterEndingString is for backwards compatibility with some early tables
	 * that appended a trailing 0 to the byte[] even though it wasn't necessary
	 */
	public static byte[] getConcatenatedValueBytes(
			Collection<Field<?>> fields,
			boolean allowNulls,
			boolean terminateIntermediateString,
			boolean terminateFinalString){
		int totalFields = fields == null ? 0 : fields.size();
		int numNonNullFields = FieldTool.countNonNullLeadingFields(fields);
		if(numNonNullFields == 0){
			return null;
		}
		byte[][] fieldArraysWithSeparators = new byte[totalFields][];
		int fieldIdx = -1;
		for(Field<?> field : fields){
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

	/**
	 * @param includePrefix usually refers to the "key." prefix before a PK
	 * @param skipNullValues important to include nulls in PK's, but usually skip them in normal fields
	 */
	public static byte[] getSerializedKeyValues(
			Collection<Field<?>> fields,
			boolean includePrefix,
			boolean skipNullValues){
		if(fields == null || fields.isEmpty()){
			return new byte[0];
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for(Field<?> field : fields){
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


	/*-------------------------------- csv ----------------------------------*/

	public static String getCsvColumnNames(Iterable<Field<?>> fields){
		StringBuilder sb = new StringBuilder();
		appendCsvColumnNames(sb, fields);
		return sb.toString();
	}

	public static void appendCsvColumnNames(StringBuilder sb, Iterable<Field<?>> fields){
		int appended = 0;
		for(Field<?> field : fields){
			if(appended > 0){
				sb.append(", ");
			}
			sb.append(field.getKey().getColumnName());
			++appended;
		}
	}

	public static List<String> getFieldNames(List<Field<?>> fields){
		List<String> fieldNames = new LinkedList<>();
		for(Field<?> field : fields){
			fieldNames.add(field.getKey().getName());
		}
		return fieldNames;
	}

	public static List<?> getFieldValues(List<Field<?>> fields){
		List<Object> fieldValues = new LinkedList<>();
		for(Field<?> field : fields){
			fieldValues.add(field.getValue());
		}
		return fieldValues;
	}

	public static Object getFieldValue(List<Field<?>> fields, String fieldName){
		for(Field<?> field : fields){
			if(field.getKey().getName().equals(fieldName)){
				return field.getValue();
			}
		}
		return null;
	}

	//prepend a new prefix to an existing prefix
	public static List<Field<?>> prependPrefixes(String prefixPrefix, List<Field<?>> fields){
		for(Field<?> field : fields){
			if(StringTool.isEmpty(field.getPrefix())){
				field.setPrefix(prefixPrefix);
			}else{
				field.setPrefix(prefixPrefix + "." + field.getPrefix());
			}
		}
		return fields;
	}


	/*----------------------------- reflection ------------------------------*/

	public static Object getNestedFieldSet(Object object, Field<?> field){
		if(StringTool.isEmpty(field.getPrefix())){
			return object;//no prefixes
		}
		String[] fieldNames = field.getPrefix().split("\\.");
		Object current = object;
		for(String fieldName : fieldNames){//return the FieldSet, not the actual Integer (or whatever) field
			current = ReflectionTool.get(fieldName, current);
		}
		return current;
	}

}