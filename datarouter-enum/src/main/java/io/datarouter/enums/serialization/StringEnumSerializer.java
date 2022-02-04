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
package io.datarouter.enums.serialization;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import io.datarouter.enums.StringEnum;

public class StringEnumSerializer<T extends Enum<T> & StringEnum<T>> implements JsonSerializer<T>, JsonDeserializer<T>{

	@Override
	public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
		@SuppressWarnings("unchecked")
		Class<T> classOfT = (Class<T>) typeOfT;
		T enumValue = classOfT.getEnumConstants()[0];
		return enumValue.fromPersistentString(json.getAsString());
	}

	@Override
	public JsonElement serialize(T stringEnum, Type typeOfSrc, JsonSerializationContext context){
		return new JsonPrimitive(stringEnum.getPersistentString());
	}

}
