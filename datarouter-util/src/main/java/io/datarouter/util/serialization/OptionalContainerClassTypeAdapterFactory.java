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
package io.datarouter.util.serialization;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.datarouter.util.lang.ReflectionTool;

/**
 * This factory creates an adaptor that correctly serialize Dtos that contain non static Optionals.
 * It is intended to be used in tandem with OptionalTypeAdapterFactory. This adapter will
 * ensure that empty Optionals are serialized and null Optionals are omitted.
 * @see OptionalTypeAdapterFactory
 */
public class OptionalContainerClassTypeAdapterFactory implements TypeAdapterFactory{

	@Override
	@SuppressWarnings("unchecked")
	public final <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type){
		Class<T> rawType = (Class<T>)type.getRawType();
		boolean hasOptionals = Arrays.stream(rawType.getDeclaredFields())
				.filter(field -> !Modifier.isStatic(field.getModifiers()))
				.anyMatch(field -> field.getType() == Optional.class);
		return hasOptionals ? (TypeAdapter<T>)getClassAdapter(gson, type) : null;
	}

	private <T> TypeAdapter<T> getClassAdapter(Gson gson, TypeToken<T> type){
		final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
		final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
		return new TypeAdapter<T>(){
			@Override
			public void write(JsonWriter out, T value) throws IOException{
				JsonElement tree = delegate.toJsonTree(value);
				processOptionalFields(value, tree);
				elementAdapter.write(out, tree);
			}

			@Override
			public T read(JsonReader in) throws IOException{
				JsonElement tree = elementAdapter.read(in);
				return delegate.fromJsonTree(tree);
			}
		};
	}

	protected <T> void processOptionalFields(T source, JsonElement toSerialize){
		JsonObject custom = toSerialize.getAsJsonObject();
		List<Field> fields = ReflectionTool.getAllHierarchyFields(source.getClass());
		fields.addAll(ReflectionTool.getAllFields(source.getClass()));
		fields.stream()
				.filter(field -> !Modifier.isStatic(field.getModifiers()))
				.filter(field -> field.getType() == Optional.class)
				.forEach(field -> {
					field.setAccessible(true);
					try{
						if(field.get(source) == null){
							custom.remove(getName(field));
						}
					}catch(IllegalArgumentException | IllegalAccessException e){
						throw new RuntimeException("Could not test Optional value", e);
					}
				});
	}

	private String getName(Field field){
		SerializedName anotation = field.getAnnotation(SerializedName.class);
		if(anotation == null){
			return field.getName();
		}
		return anotation.value();
	}

}