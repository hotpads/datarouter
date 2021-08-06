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

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.bytes.ByteReader;
import io.datarouter.util.lang.ReflectionTool;

public class FieldSetTool{

	public static <F extends FieldSet<F>> F clone(F fieldSet){
		@SuppressWarnings("unchecked")
		F copy = (F)ReflectionTool.create(fieldSet.getClass());
		fieldSet.getFields().forEach(field -> field.setUsingReflection(copy, field.getValue()));
		return copy;
	}

	public static <F> F fieldSetFromBytes(
			Supplier<F> supplier,
			Map<String,Field<?>> fieldByPrefixedName,
			byte[] bytes){
		F fieldSet = supplier.get();
		var reader = new ByteReader(bytes);
		while(reader.hasMore()){
			String prefixedName = reader.varUtf8();
			byte[] valueBytes = reader.varBytes();
			Field<?> field = fieldByPrefixedName.get(prefixedName);
			if(field != null){
				Object value = field.fromBytesButDoNotSet(valueBytes, 0);
				field.setUsingReflection(fieldSet, value);
			}
		}
		return fieldSet;
	}

	public static <F extends FieldSet<?>> F fromConcatenatedValueBytes(
			Supplier<F> cls,
			List<Field<?>> fields,
			byte[] bytes){
		F fieldSet = cls.get();
		if(ArrayTool.isEmpty(bytes)){
			return fieldSet;
		}
		int byteOffset = 0;
		for(Field<?> field : fields){
			if(byteOffset == bytes.length){// ran out of bytes. leave remaining fields blank
				break;
			}
			int numBytesWithSeparator = field.numBytesWithSeparator(bytes, byteOffset);
			Object value;
			try{
				value = field.fromBytesWithSeparatorButDoNotSet(bytes, byteOffset);
			}catch(Exception e){
				throw new RuntimeException("could not decode class=" + cls.get().getClass().getName()
						+ " field=" + field
						+ " offset=" + byteOffset
						+ " bytes=" + Base64.getEncoder().encodeToString(bytes),
						e);
			}
			field.setUsingReflection(fieldSet, value);
			byteOffset += numBytesWithSeparator;
		}
		return fieldSet;
	}

}
