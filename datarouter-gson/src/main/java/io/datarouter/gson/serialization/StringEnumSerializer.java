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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import io.datarouter.enums.StringEnum;

/**
 * @deprecated  Use StringMappedEnumSerializer
 */
@Deprecated
public class StringEnumSerializer<T extends Enum<T> & StringEnum<T>>
implements JsonSerializer<T>, JsonDeserializer<T>{
	private static final Logger logger = LoggerFactory.getLogger(StringEnumSerializer.class);

	private static final Set<String> deserialized = ConcurrentHashMap.newKeySet();
	private static final Set<String> serialized = ConcurrentHashMap.newKeySet();

	private final String parentName;//for tracking usage
	private final Boolean hierarchy;

	public StringEnumSerializer(String parentName, boolean hierarchy){
		this.parentName = parentName;
		this.hierarchy = hierarchy;
	}

	@Override
	public JsonElement serialize(T stringEnum, Type type, JsonSerializationContext context){
		String typeName = type.getTypeName();
		if(serialized.add(typeName)){
			logger.warn("serialized {}", makeMessage(typeName));
		}
		return new JsonPrimitive(stringEnum.getPersistentString());
	}

	@Override
	public T deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException{
		String typeName = type.getTypeName();
		if(deserialized.add(typeName)){
			logger.warn("deserialized {}", makeMessage(typeName));
		}
		@SuppressWarnings("unchecked")
		Class<T> classOfT = (Class<T>) type;
		T enumValue = classOfT.getEnumConstants()[0];
		return enumValue.fromPersistentString(json.getAsString());
	}

	private String makeMessage(String typeName){
		return String.format(
				"enumClass=%s parentName=%s hierarchy=%s.  Please replace with %s",
				typeName,
				Optional.ofNullable(parentName).orElse("null"),
				Optional.ofNullable(hierarchy).map(Object::toString).orElse("null"),
				StringMappedEnumSerializer.class.getSimpleName());
	}

}
