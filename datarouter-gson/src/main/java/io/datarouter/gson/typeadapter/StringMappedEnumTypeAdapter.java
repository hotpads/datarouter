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
package io.datarouter.gson.typeadapter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import io.datarouter.enums.MappedEnum;

public class StringMappedEnumTypeAdapter<T extends Enum<T>>
extends TypeAdapter<T>{
	private static final Logger logger = LoggerFactory.getLogger(StringMappedEnumTypeAdapter.class);

	private final MappedEnum<T,String> mappedEnum;
	private final boolean optional;
	private final T defaultValue;
	private final boolean logMissingValues;

	private StringMappedEnumTypeAdapter(
			MappedEnum<T,String> mappedEnum,
			boolean optional,
			T defaultValue,
			boolean logMissingValues){
		this.mappedEnum = mappedEnum;
		this.optional = optional;
		this.defaultValue = defaultValue;
		this.logMissingValues = logMissingValues;
	}

	/**
	 * When deserializing, throw an exception if the value is not found.
	 */
	public static <T extends Enum<T>> StringMappedEnumTypeAdapter<T> required(MappedEnum<T,String> mappedEnum){
		return new StringMappedEnumTypeAdapter<>(mappedEnum, false, null, false);
	}

	/**
	 * When deserializing, return the replacement if the value is not found.
	 */
	public static <T extends Enum<T>> StringMappedEnumTypeAdapter<T> optionalWithoutLogging(
			MappedEnum<T,String> mappedEnum,
			T defaultValue){
		return new StringMappedEnumTypeAdapter<>(mappedEnum, true, defaultValue, false);
	}

	/**
	 * When deserializing, return the replacement if the value is not found.  Log the replacement.
	 */
	public static <T extends Enum<T>> StringMappedEnumTypeAdapter<T> optionalWithLogging(
			MappedEnum<T,String> mappedEnum,
			T replacement){
		return new StringMappedEnumTypeAdapter<>(mappedEnum, true, replacement, true);
	}

	@Override
	public void write(JsonWriter out, T value) throws IOException{
		String stringValue = value == null ? null : mappedEnum.toKey(value);
		out.value(stringValue);
	}

	@Override
	public T read(JsonReader in) throws IOException{
		if(in.peek() == JsonToken.NULL){
			in.nextNull();
			return null;
		}
		String stringValue = in.nextString();
		if(!optional){
			return mappedEnum.fromOrThrow(stringValue);
		}
		T value = mappedEnum.fromOrNull(stringValue);
		if(value != null){
			return value;
		}
		if(logMissingValues){
			logger.warn("Unknown enum value.  Consider adding it to the enum or throwing an exception by"
					+ " registering this class using the required(..) method.  input={}[{}], returning={}",
					mappedEnum.getEnumClass().getCanonicalName(),
					stringValue,
					defaultValue);
		}
		return defaultValue;
	}

}
