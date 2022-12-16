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
import io.datarouter.bytes.codec.stringcodec.StringCodec;

/**
 * Create a single object that can convert a single class to and from byte-array encoded json.
 */
public class BinaryGsonCodec<T> implements Codec<T,byte[]>{

	private static final StringCodec STRING_CODEC = StringCodec.UTF_8;

	private final Gson gson;
	private final Class<T> cls;

	public BinaryGsonCodec(Gson gson, Class<T> cls){
		this.gson = gson;
		this.cls = cls;
	}

	@Override
	public byte[] encode(T value){
		String json = gson.toJson(value);
		return STRING_CODEC.encode(json);
	}

	@Override
	public T decode(byte[] encodedValue){
		String json = STRING_CODEC.decode(encodedValue);
		return gson.fromJson(json, cls);
	}

}
