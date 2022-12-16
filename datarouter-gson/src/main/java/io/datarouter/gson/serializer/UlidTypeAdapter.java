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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import io.datarouter.types.Ulid;

public class UlidTypeAdapter implements JsonSerializer<Ulid>, JsonDeserializer<Ulid>{

	@Override
	public Ulid deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
	throws JsonParseException{
		JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
		return new Ulid(jsonPrimitive.getAsString());
	}

	@Override
	public JsonElement serialize(Ulid src, Type typeOfSrc, JsonSerializationContext context){
		return new JsonPrimitive(src.value());
	}

}
