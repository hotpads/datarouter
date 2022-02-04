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

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import io.datarouter.gson.Java11;

/**
 * This factory creates an adaptor that correctly serialize Optionals.
 * @see OptionalContainerClassTypeAdapterFactory
 */
public class OptionalTypeAdapterFactory implements TypeAdapterFactory{

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken){
		if(typeToken.getRawType() != Optional.class){
			return null;
		}
		Type actualType = ((ParameterizedType)typeToken.getType()).getActualTypeArguments()[0];
		TypeAdapter<?> typeAdapter = gson.getAdapter(TypeToken.get(actualType));
		return new OptionalTypeAdapter(typeAdapter);
	}

	private static class OptionalTypeAdapter<E> extends TypeAdapter<Optional<E>>{

		private final TypeAdapter<E> typeAdapter;

		public OptionalTypeAdapter(TypeAdapter<E> typeAdapter){
			this.typeAdapter = typeAdapter;
		}

		@Override
		public void write(JsonWriter out, Optional<E> value) throws IOException{
			if(value == null || Java11.isEmpty(value)){
				out.nullValue();
			}else{
				typeAdapter.write(out, value.get());
			}
		}

		@Override
		public Optional<E> read(JsonReader in) throws IOException{
			if(in.peek() != JsonToken.NULL){
				return Optional.ofNullable(typeAdapter.read(in));
			}
			in.nextNull();
			return Optional.empty();
		}
	}

}
