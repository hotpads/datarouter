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
package io.datarouter.gson.typeadapterfactory;

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
import io.datarouter.gson.typeadapter.StringMappedEnumTypeAdapter;

/**
 * Intercepts all enum serialization, preventing accidental serialization of enums without explicitly specifying
 * an encoding.
 *
 * This will reject unknown enums by default but can allow them by setting allowUnregistered()
 */
public abstract class EnumTypeAdapterFactory
implements TypeAdapterFactory{
	private static final Logger logger = LoggerFactory.getLogger(EnumTypeAdapterFactory.class);

	private static final Set<String> loggedTypes = ConcurrentHashMap.newKeySet();

	private final Map<Type,TypeAdapter<?>> adapterByType = new HashMap<>();
	private boolean allowUnregistered = false;
	private boolean logUnregistered = true;

	/**
	 * Not recommended.
	 * Instead of throwing an exception for unregistered Enum types, let Gson serialize by enum.name() or annotation
	 */
	protected EnumTypeAdapterFactory allowUnregistered(){
		allowUnregistered = true;
		return this;
	}

	/**
	 * Not recommended.
	 * Suppress the warning logs when unregistered enums are encountered, for cases where the enums can't be known
	 * about ahead of time.
	 */
	protected EnumTypeAdapterFactory suppressUnregisteredLogging(){
		logUnregistered = false;
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
				if(logUnregistered && loggedTypes.add(type.toString())){
					String format = "Warning: Currently serializing missingEnum=%s/%s by Enum.name()"
							+ " or @SerializedName.  Please register a TypeAdapter.";
					String message = String.format(format, getClass().getCanonicalName(), type);
					logger.warn(message, new Exception());
				}
			}else{
				String message = String.format(
						"Error: please register a TypeAdapter for rejectedEnum=%s/%s",
						getClass().getCanonicalName(),
						type);
				throw new IllegalArgumentException(message);
			}
		}
		return typeAdapter;
	}

	/**
	 * This can be used to intercept and log unregistered enums, however when multiple of these exist, the logs will
	 * not differentiate between them.  A more robust solution is to create specific ones with unique class names.
	 */
	public static class AnonymousAllowUnregisteredEnumTypeAdapterFactory extends EnumTypeAdapterFactory{

		public static final AnonymousAllowUnregisteredEnumTypeAdapterFactory INSTANCE
				= new AnonymousAllowUnregisteredEnumTypeAdapterFactory();

		public AnonymousAllowUnregisteredEnumTypeAdapterFactory(){
			allowUnregistered();
		}

	}

	public static class RejectAllEnumTypeAdapterFactory extends EnumTypeAdapterFactory{

		public static final RejectAllEnumTypeAdapterFactory INSTANCE = new RejectAllEnumTypeAdapterFactory();

	}

}