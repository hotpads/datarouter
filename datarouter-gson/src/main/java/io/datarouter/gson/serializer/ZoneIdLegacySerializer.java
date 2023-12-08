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
import java.time.ZoneId;
import java.time.ZoneOffset;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ZoneIdLegacySerializer implements JsonSerializer<ZoneId>{

	@Override
	public JsonElement serialize(ZoneId zoneId, Type typeOfSrc, JsonSerializationContext context){
		JsonObject serialized = new JsonObject();
		if(zoneId instanceof ZoneOffset offset){
			serialized.addProperty("totalSeconds", offset.getTotalSeconds());
		}else{
			serialized.addProperty("id", zoneId.getId());
		}
		return serialized;
	}

}
