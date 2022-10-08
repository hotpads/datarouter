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
package io.datarouter.gson.serialization;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import io.datarouter.enums.MappedEnum;

/**
 * Intercepts all enum serialization, preventing accidental serialization of enums without explicitly specifying
 * an encoding.
 *
 * This will log unknown enums by default but can throw an exception if configured with rejectUnregistered()
 */
public abstract class EnumTypeAdapterFactory implements TypeAdapterFactory{
	private static final Logger logger = LoggerFactory.getLogger(EnumTypeAdapterFactory.class);

	private static final Set<String> loggedTypes = ConcurrentHashMap.newKeySet();

	private final Map<Type,TypeAdapter<?>> adapterByType = new HashMap<>();
	private boolean allowUnregistered = false;

	/**
	 * Not recommended.
	 * Instead of throwing an exception for unregistered Enum types, let Gson serialize them by enum.name()
	 */
	protected EnumTypeAdapterFactory allowUnregistered(){
		allowUnregistered = true;
		return this;
	}

	protected <T> EnumTypeAdapterFactory register(Class<T> cls, TypeAdapter<T> typeAdapter){
		if(!cls.isEnum()){
			String message = String.format("%s is not an enum", cls.getCanonicalName());
			throw new IllegalArgumentException(message);
		}
		adapterByType.put(cls, typeAdapter);
		return this;
	}

	/*----------- String MappedEnum convenience methods ------------*/

	protected <T extends Enum<T>> EnumTypeAdapterFactory registerStringMappedEnumRequired(
			MappedEnum<T,String> mappedEnum){
		adapterByType.put(mappedEnum.getEnumClass(), StringMappedEnumTypeAdapter.required(mappedEnum));
		return this;
	}

	protected <T extends Enum<T>> EnumTypeAdapterFactory registerStringMappedEnumOptional(
			MappedEnum<T,String> mappedEnum,
			T replacement){
		adapterByType.put(mappedEnum.getEnumClass(), StringMappedEnumTypeAdapter.optional(mappedEnum, replacement));
		return this;
	}

	protected <T extends Enum<T>> EnumTypeAdapterFactory registerStringMappedEnumOptionalWithLogging(
			MappedEnum<T,String> mappedEnum,
			T replacement){
		adapterByType.put(
				mappedEnum.getEnumClass(),
				StringMappedEnumTypeAdapter.optionalWithLogging(mappedEnum, replacement));
		return this;
	}

	/*----------- TypeAdapterFactory -------------*/

	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type){
		if(!type.getRawType().isEnum()){
			return null;
		}
		@SuppressWarnings("unchecked")
		TypeAdapter<T> typeAdapter = (TypeAdapter<T>)adapterByType.get(type.getType());
		if(typeAdapter == null){
			if(allowUnregistered){
				if(loggedTypes.add(type.toString())){
					String format = "Warning: Currently serializing missingEnum=%s by Enum.name()."
							+ "Please register a TypeAdapter in %s.";
					String message = String.format(format, type, getClass().getCanonicalName());
					logger.warn(message);
				}
			}else{
				String message = String.format(
						"Error: please register a TypeAdapter for rejectedEnum=%s in %s",
						type,
						getClass().getCanonicalName());
				throw new IllegalArgumentException(message);
			}
		}
		return typeAdapter;
	}

}