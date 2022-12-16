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
package io.datarouter.gson.codec;

import com.google.gson.Gson;

import io.datarouter.bytes.Codec;

/**
 * Create a single object that can convert a single class to and from json.
 */
public class GsonCodec<T> implements Codec<T,String>{

	private final Gson gson;
	private final Class<T> cls;

	public GsonCodec(Gson gson, Class<T> cls){
		this.gson = gson;
		this.cls = cls;
	}

	@Override
	public String encode(T value){
		return gson.toJson(value);
	}

	@Override
	public T decode(String encodedValue){
		return gson.fromJson(encodedValue, cls);
	}

}
