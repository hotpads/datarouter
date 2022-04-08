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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.datarouter.model.exception.DataAccessException;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.string.StringTool;

public abstract class BaseField<T> implements Field<T>{

	private static final Map<Class<?>,Map<String,java.lang.reflect.Field>> CACHED_FIELDS = new ConcurrentHashMap<>();

	private String prefix;// ignore if not needed
	protected T value;

	/*---------------------------- constructor ------------------------------*/

	public BaseField(String prefix, T value){
		this.prefix = StringTool.nullSafe(prefix);
		this.value = value;
	}

	/*------------------------------ methods --------------------------------*/

	@Override
	public int getValueHashCode(){
		return value == null ? 0 : value.hashCode();
	}

	@Override
	public String toString(){
		return getPrefixedName() + ":" + getValueString();
	}

	@Override
	public void fromString(String valueAsString){
		this.value = parseStringEncodedValueButDoNotSet(valueAsString);
	}

	/*----------------------------- ByteField -------------------------------*/

	@Override
	public byte[] getBytesWithSeparator(){
		return getBytes();
	}

	@Override
	public T fromBytesWithSeparatorButDoNotSet(byte[] bytes, int byteOffset){
		return fromBytesButDoNotSet(bytes, byteOffset);
	}

	/*------------------------- reflective setters --------------------------*/

	@Override
	public void setUsingReflection(Object targetFieldSet, Object fieldValue){
		try{
			Object nestedFieldSet = FieldTool.getNestedFieldSet(targetFieldSet, this);
			java.lang.reflect.Field javaField = CACHED_FIELDS
					.computeIfAbsent(nestedFieldSet.getClass(), $ -> new ConcurrentHashMap<>())
					.get(getKey().getName());
			if(javaField == null){
				javaField = ReflectionTool.getDeclaredFieldFromAncestors(nestedFieldSet.getClass(), getKey().getName());
				if(javaField == null){
					String message = String.format("field=%s doesn't exist in %s",
							getKey().getName(),
							nestedFieldSet.getClass());
					throw new RuntimeException(message);
				}
				CACHED_FIELDS.get(nestedFieldSet.getClass()).put(getKey().getName(), javaField);
			}
			javaField.set(nestedFieldSet, fieldValue);
		}catch(Exception e){
			String message = e.getClass().getSimpleName()
					+ " on " + targetFieldSet.getClass().getSimpleName() + "." + getKey().getName();
			throw new DataAccessException(message, e);
		}
	}

	@Override
	public String getPrefixedName(){
		if(StringTool.isEmpty(prefix)){
			return getKey().getName();
		}
		return prefix + "." + getKey().getName();
	}

	@Override
	public String getPrefix(){
		return prefix;
	}

	@Override
	public Field<T> setPrefix(String prefix){
		this.prefix = prefix;
		return this;
	}

	@Override
	public Field<T> setValue(T value){
		this.value = value;
		return this;
	}

	@Override
	public T getValue(){
		return value;
	}

	@Override
	public String getValueString(){
		return String.valueOf(value);
	}

	@Override
	public String getPreparedStatementValue(){
		return "?";
	}

}
