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

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * For rejecting enums without an explicit TypeAdapter. This helps enforce that enums are serialized conscientiously,
 * even if the developer chooses to serialize the enum name.
 */
public class RejectEnumsTypeAdapter<E extends Enum<E>>
extends TypeAdapter<E>{

	public static class RejectEnumsTypeAdapterFactory implements TypeAdapterFactory{

		@SuppressWarnings({"rawtypes", "unchecked"})
		@Override
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type){
			if(type.getRawType().isEnum()){
				return new RejectEnumsTypeAdapter(type);
			}
			return null;
		}

	}

	private final TypeToken<E> type;

	public RejectEnumsTypeAdapter(TypeToken<E> type){
		this.type = type;
	}

	@Override
	public void write(JsonWriter out, E value){
		throw makeException();
	}

	@Override
	public E read(JsonReader in){
		throw makeException();
	}

	private IllegalArgumentException makeException(){
		String message = String.format("Please register an explicit TypeAdapter for %s", type);
		throw new IllegalArgumentException(message);
	}

}
