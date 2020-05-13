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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.bytes.StringByteTool;
import io.datarouter.util.lang.ObjectTool;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.tuple.Pair;
import io.datarouter.util.varint.VarInt;

public class FieldSetTool{

	public static <F extends FieldSet<F>> F clone(F fieldSet){
		@SuppressWarnings("unchecked")
		F copy = (F)ReflectionTool.create(fieldSet.getClass());
		fieldSet.getFields().forEach(field -> field.setUsingReflection(copy, field.getValue()));
		return copy;
	}

	public static int getNumNonNullLeadingFields(FieldSet<?> prefix){
		int numNonNullFields = 0;
		for(Object value : prefix.getFieldValues()){
			if(value == null){
				break;
			}
			++numNonNullFields;
		}
		return numNonNullFields;
	}

	public static int getNumNonNullFields(FieldSet<?> prefix){
		int numNonNullFields = 0;
		for(Object value : prefix.getFieldValues()){
			if(value != null){
				++numNonNullFields;
			}
		}
		return numNonNullFields;
	}

	public static boolean areAllFieldsNonNull(FieldSet<?> fieldSet){
		if(fieldSet == null){
			return false;
		}
		return getNumNonNullFields(fieldSet) == fieldSet.getFields().size();
	}

	public static Map<String,Pair<Field<?>,Field<?>>> getFieldDifferences(Collection<Field<?>> left,
			Collection<Field<?>> right){

		Map<String,Pair<Field<?>,Field<?>>> diffMap = new HashMap<>();

		Map<String,Field<?>> leftMap = generateFieldMap(left);
		Map<String,Field<?>> rightMap = generateFieldMap(right);
		for(String key : Scanner.concat(leftMap.keySet(), rightMap.keySet()).collect(HashSet::new)){
			Field<?> leftField = leftMap.get(key);
			Field<?> rightField = rightMap.get(key);

			if(ObjectTool.isOneNullButNotTheOther(leftField, rightField)
					|| ObjectTool.notEquals(leftField.getValue(), rightField.getValue())){
				diffMap.put(key, new Pair<>(leftField, rightField));
			}
		}

		return diffMap;
	}

	public static Map<String,Field<?>> generateFieldMap(Collection<Field<?>> fields){
		Map<String,Field<?>> fieldMap = new TreeMap<>();
		if(fields == null){
			return fieldMap;
		}

		Iterator<Field<?>> fieldIter = fields.iterator();
		while(fieldIter.hasNext()){
			Field<?> field = fieldIter.next();
			fieldMap.put(field.getKey().getName(), field);
		}
		return fieldMap;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>Map<PK,List<Field<?>>> getFieldsByKey(
			Iterable<D> databeans, DatabeanFielder<PK,D> fielder){
		Map<PK,List<Field<?>>> fieldsByKey = new HashMap<>();
		for(D databean : databeans){
			fieldsByKey.put(databean.getKey(), fielder.getFields(databean));
		}
		return fieldsByKey;
	}


	/*----------------- construct fieldsets using reflection ----------------*/

	public static <F> F fieldSetFromByteStream(Supplier<F> supplier, Map<String,Field<?>> fieldByPrefixedName,
			InputStream is) throws IOException{
		int databeanLength = VarInt.fromInputStream(is).getValue();
		return fieldSetFromByteStreamKnownLength(supplier, fieldByPrefixedName, is, databeanLength);
	}

	public static <F> F fieldSetFromBytes(Supplier<F> supplier, Map<String,Field<?>> fieldByPrefixedName,
			byte[] bytes) throws IOException{
		return fieldSetFromByteStreamKnownLength(supplier, fieldByPrefixedName, new ByteArrayInputStream(bytes),
				bytes.length);
	}

	public static <F> F fieldSetFromByteStreamKnownLength(Supplier<F> supplier,
			Map<String,Field<?>> fieldByPrefixedName, InputStream is, int numBytes) throws IOException{
		F targetFieldSet = supplier.get();
		int numBytesThroughDatabean = 0;
		while(true){
			//will throw IllegalArgumentException at the end of the stream
			VarInt nameLength = VarInt.fromInputStream(is);
			byte[] nameBytes = new byte[nameLength.getValue()];
			is.read(nameBytes);
			numBytesThroughDatabean += nameLength.getNumBytes() + nameLength.getValue();
			String prefixedName = StringByteTool.fromUtf8Bytes(nameBytes);
			Field<?> field = fieldByPrefixedName.get(prefixedName);
			if(field == null){
				continue;
			}
			VarInt valueLength = VarInt.fromInputStream(is);
			numBytesThroughDatabean += valueLength.getNumBytes();
			byte[] valueBytes = new byte[valueLength.getValue()];
			if(valueLength.getValue() > 0){
				is.read(valueBytes);
				numBytesThroughDatabean += valueLength.getValue();
			}
			Object value = field.fromBytesButDoNotSet(valueBytes, 0);
			field.setUsingReflection(targetFieldSet, value);
			if(numBytesThroughDatabean >= numBytes){
				break;
			}
		}
		return targetFieldSet;
	}

	public static <F extends FieldSet<?>> F fromConcatenatedValueBytes(Class<F> cls, List<Field<?>> fields,
			byte[] bytes){
		F fieldSet = ReflectionTool.create(cls);
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
				throw new RuntimeException("could not decode class=" + cls.getName() + " field=" + field + " offset="
						+ byteOffset + " bytes=" + Base64.getEncoder().encodeToString(bytes), e);
			}
			field.setUsingReflection(fieldSet, value);
			byteOffset += numBytesWithSeparator;
		}

		return fieldSet;
	}

}
