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
package io.datarouter.model.field.codec;

import java.util.Comparator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.datarouter.bytes.Codec.NullPassthroughCodec;

public class GsonFieldCodec<T> extends FieldCodec<T,String>{

	public GsonFieldCodec(Gson gson, Class<T> dtoClass, T sampleValue){
		super(TypeToken.get(dtoClass),
				NullPassthroughCodec.of(gson::toJson, string -> gson.fromJson(string, dtoClass)),
				Comparator.comparing(gson::toJson),
				sampleValue,
				null);
	}

	public GsonFieldCodec(Gson gson, Class<T> dtoClass){
		this(gson, dtoClass, null);// SampleValue would ideally be provided.
	}

}
