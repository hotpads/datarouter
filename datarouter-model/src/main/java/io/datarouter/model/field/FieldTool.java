/*
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

import java.util.ArrayList;
import java.util.List;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.bytes.varint.VarIntTool;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.string.StringTool;

public class FieldTool{

	public static int countNonNullLeadingFields(List<Field<?>> fields){
		int num = 0;
		while(num < fields.size() && fields.get(num).getValue() != null){
			++num;
		}
		return num;
	}

	/*
	 * @deprecated  use getConcatenatedValueBytesTerminated which allows '0' bytes in Strings.
	 */
	@Deprecated
	public static byte[] getConcatenatedValueBytes(List<Field<?>> fields){
		int numTokens = FieldTool.countNonNullLeadingFields(fields);
		if(numTokens == 0){
			return EmptyArray.BYTE;
		}
		byte[][] tokens = new byte[numTokens][];
		for(int i = 0; i < numTokens; ++i){
			tokens[i] = fields.get(i).getKeyBytesWithSeparator();
		}
		return ByteTool.concat(tokens);
	}

	public static byte[] getConcatenatedValueBytesTerminated(List<Field<?>> fields){
		int numTokens = FieldTool.countNonNullLeadingFields(fields);
		if(numTokens == 0){
			return EmptyArray.BYTE;
		}
		byte[][] tokens = new byte[numTokens][];
		for(int i = 0; i < numTokens; ++i){
			tokens[i] = fields.get(i).getTerminatedKeyBytes();
		}
		return ByteTool.concat(tokens);
	}

	/**
	 * @deprecated  Use a dedicated codec for each use case.
	 * @param includePrefix usually refers to the "key." prefix before a PK
	 * @param skipNullValues important to include nulls in PK's, but usually skip them in normal fields
	 */
	@Deprecated
	public static byte[] getSerializedKeyValues(
			List<Field<?>> fields,
			boolean includePrefix,
			boolean skipNullValues){
		List<byte[]> tokens = new ArrayList<>(4 * fields.size());
		for(Field<?> field : fields){
			byte[] value = field.getValueBytes();
			if(value == null && skipNullValues){
				continue;
			}
			byte[] key = includePrefix
					? StringCodec.UTF_8.encode(field.getPrefixedName())
					: field.getKey().getColumnNameBytes();
			tokens.add(VarIntTool.encode(key.length));
			tokens.add(key);
			tokens.add(VarIntTool.encode(value.length));
			tokens.add(value);
		}
		return ByteTool.concat(tokens);
	}

	//prepend a new prefix to an existing prefix
	public static List<Field<?>> prependPrefixes(String prefixPrefix, List<Field<?>> fields){
		fields.forEach(field -> {
			if(StringTool.isEmpty(field.getPrefix())){
				field.setPrefix(prefixPrefix);
			}else{
				field.setPrefix(prefixPrefix + "." + field.getPrefix());
			}
		});
		return fields;
	}

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
