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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import io.datarouter.json.JsonSerializer;

@Singleton
public class GsonJsonSerializer implements JsonSerializer{

	public static final GsonJsonSerializer DEFAULT = new GsonJsonSerializer(GsonTool.GSON);

	private final Gson gson;

	@Inject
	public GsonJsonSerializer(Gson gson){
		this.gson = gson;
	}

	@Override
	public String serialize(Object toSerialize){
		return toJson(toSerialize);
	}

	public String toJson(Object toSerialize){
		try{
			return gson.toJson(toSerialize);
		}catch(IllegalArgumentException e){
			throw new IllegalArgumentException("error class=" + toSerialize.getClass().getName()
					+ " " + describeFields(toSerialize), e);
		}
	}

	private static String describeFields(Object object){
		return Arrays.stream(object.getClass().getDeclaredFields())
				.map(field ->
						field.getGenericType()
						+ "-" + field.getName()
						+ "=" + Optional.ofNullable(get(field, object))
								.map(Object::getClass)
								.map(Class::getName)
								.orElse(null))
				.collect(Collectors.joining(" "));
	}

	private static Object get(Field field, Object object){
		field.setAccessible(true);
		try{
			return field.get(object);
		}catch(IllegalArgumentException | IllegalAccessException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> T deserialize(String toDeserialize, Type returnType){
		return fromJson(toDeserialize, returnType);
	}

	public <T> T fromJson(String toDeserialize, Type returnType){
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
