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
package io.datarouter.bytes.codec.list.bytearraylist;

import io.datarouter.bytes.ByteArrays;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.LengthAndValue;

/**
 * VarInt size
 * VarInt for the length of each array
 * The concatenated arrays
 */
public class ByteArraysCodec implements Codec<ByteArrays,byte[]>{

	public static final ByteArraysCodec INSTANCE = new ByteArraysCodec();

	@Override
	public byte[] encode(ByteArrays value){
		return value.toBytes();
	}

	@Override
	public ByteArrays decode(byte[] bytes){
		return decode(bytes, 0).value;
	}

	public LengthAndValue<ByteArrays> decode(byte[] bytes, int offset){
		var value = ByteArrays.of(bytes, offset);
		int length = value.getLength();
		return new LengthAndValue<>(length, value);
	}

}
