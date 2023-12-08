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
package io.datarouter.gson.serializer;

import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import io.datarouter.enums.MappedEnum;

public class IntegerMappedEnumSerializer<T extends Enum<T>>
implements JsonSerializer<T>, JsonDeserializer<T>{
	private static final Logger logger = LoggerFactory.getLogger(IntegerMappedEnumSerializer.class);

	private final MappedEnum<T,Integer> mappedEnum;
	private final boolean optional;
	private final T replacement;
	private final boolean logMissingValues;

	private IntegerMappedEnumSerializer(
			MappedEnum<T,Integer> mappedEnum,
			boolean optional,
			T replacement,
			boolean logMissingValues){
		this.mappedEnum = mappedEnum;
		this.optional = optional;
		this.replacement = replacement;
		this.logMissingValues = logMissingValues;
	}

	/**
	 * When deserializing, throw an exception if the value is not found.
	 */
	public static <T extends Enum<T>> IntegerMappedEnumSerializer<T> required(MappedEnum<T,Integer> mappedEnum){
		return new IntegerMappedEnumSerializer<>(mappedEnum, false, null, false);
	}

	/**
	 * When deserializing, return the replacement if the value is not found.
	 */
	public static <T extends Enum<T>> IntegerMappedEnumSerializer<T> optional(
			MappedEnum<T,Integer> mappedEnum,
			T replacement){
		return new IntegerMappedEnumSerializer<>(mappedEnum, true, replacement, false);
	}

	/**
	 * When deserializing, return the replacement if the value is not found.  Log the replacement.
	 */
	public static <T extends Enum<T>> IntegerMappedEnumSerializer<T> optionalWithLogging(
			MappedEnum<T,Integer> mappedEnum,
			T replacement){
		return new IntegerMappedEnumSerializer<>(mappedEnum, true, replacement, true);
	}

	@Override
	public JsonElement serialize(T enumValue, Type typeOfSrc, JsonSerializationContext context){
		return new JsonPrimitive(mappedEnum.toKey(enumValue));
	}

	@Override
	public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
		int intValue = json.getAsInt();
		if(optional){
			T value = mappedEnum.fromOrNull(intValue);
			if(value != null){
				return value;
			}else{
				if(logMissingValues){
					logger.warn("Unknown enum value.  Consider adding it to the enum or throwing an exception by"
							+ " registering this class using the required(..) method.  input={}[{}], returning={}",
							mappedEnum.getEnumClass().getCanonicalName(),
							intValue,
							replacement);
				}
				return replacement;
			}
		}else{
			return mappedEnum.fromOrThrow(intValue);
		}
	}

}
