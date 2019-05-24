/**
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
package io.datarouter.httpclient.json;

import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

@Singleton
public class GsonJsonSerializer implements JsonSerializer{

	private final Gson gson;

	@Inject
	public GsonJsonSerializer(Gson gson){
		this.gson = gson;
	}

	@Override
	public <T> String serialize(T toSerialize){
		return gson.toJson(toSerialize);
	}

	@Override
	public <T> T deserialize(String toDeserialize, Type returnType){
		try{
			return gson.fromJson(toDeserialize, returnType);
		}catch(JsonSyntaxException e){
			throw new JsonSyntaxException("Json syntax exception for string=\"" + toDeserialize + "\"", e);
		}catch(JsonParseException e){
			throw new JsonParseException("Failed to deserialize string=\"" + toDeserialize + "\" to type="
					+ returnType, e);
		}
	}

}
