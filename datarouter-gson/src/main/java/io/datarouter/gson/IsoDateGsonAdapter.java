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
package io.datarouter.gson;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class IsoDateGsonAdapter implements JsonSerializer<Date>, JsonDeserializer<Date>{

	@Override
	public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context){
		String dateString = json.getAsString();
		if(dateString == null || dateString.isEmpty()){
			return null;
		}
		return Date.from(Instant.parse(dateString));
	}

	@Override
	public JsonElement serialize(Date date, Type typeOfSrc, JsonSerializationContext context){
		if(date == null){
			return JsonNull.INSTANCE;
		}
		return new JsonPrimitive(DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(date.getTime())));
	}

}