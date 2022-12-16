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
package io.datarouter.gson.typeadapterfactory;

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

public class OptionalLegacyTypeAdapterFactory
implements TypeAdapterFactory{

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken){
		if(typeToken.getRawType() != Optional.class){
			return null;
		}
		return new OptionalLegacyTypeAdapter(gson, typeToken);
	}

	public class OptionalLegacyTypeAdapter<E> extends TypeAdapter<Optional<E>>{

		private final Gson gson;
		private final TypeToken<Optional<E>> typeToken;

		public OptionalLegacyTypeAdapter(Gson gson, TypeToken<Optional<E>> typeToken){
			this.gson = gson;
			this.typeToken = typeToken;
		}

		@Override
		public void write(JsonWriter out, Optional<E> value) throws IOException{
			if(value == null){
				out.nullValue();
				return;
			}
			out.beginObject().name("value");
			if(value.isPresent()){
				@SuppressWarnings("unchecked")
				TypeAdapter<E> typeAdapter = (TypeAdapter<E>)gson.getAdapter(value.get().getClass());
				typeAdapter.write(out, value.get());
			}else{
				out.nullValue();
			}
			out.endObject();
		}

		@Override
		public Optional<E> read(JsonReader in) throws IOException{
			if(in.peek() == JsonToken.NULL){
				in.nextNull();
				return null;
			}
			in.beginObject();
			E element = null;
			if(in.peek() == JsonToken.NAME){
				String name = in.nextName();
				if(!"value".equals(name)){
					throw new RuntimeException(name);
				}
				Type actualType = ((ParameterizedType)typeToken.getType()).getActualTypeArguments()[0];
				@SuppressWarnings("unchecked")
				TypeAdapter<E> typeAdapter = (TypeAdapter<E>)gson.getAdapter(TypeToken.get(actualType));
				element = typeAdapter.read(in);
			}
			in.endObject();
			return Optional.ofNullable(element);
		}

	}

}
