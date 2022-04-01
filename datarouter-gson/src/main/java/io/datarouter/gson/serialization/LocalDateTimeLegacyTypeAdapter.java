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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LocalDateTimeLegacyTypeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime>{

	@Override
	public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
	throws JsonParseException{
		JsonObject jsonObject = json.getAsJsonObject();
		return LocalDateTime.of(
				context.deserialize(jsonObject.get("date").getAsJsonObject(), LocalDate.class),
				context.deserialize(jsonObject.get("time").getAsJsonObject(), LocalTime.class));
	}

	@Override
	public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context){
		JsonObject serialized = new JsonObject();
		serialized.add("date", context.serialize(src.toLocalDate()));
		serialized.add("time", context.serialize(src.toLocalTime()));
		return serialized;
	}

}