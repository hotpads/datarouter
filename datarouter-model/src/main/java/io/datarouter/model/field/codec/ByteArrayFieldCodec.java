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

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gson.reflect.TypeToken;

public class ByteArrayFieldCodec<T> extends FieldCodec<T,byte[]>{

	public ByteArrayFieldCodec(
			TypeToken<T> typeToken,
			Supplier<byte[]> encodeNullTo,
			Function<T,byte[]> encoder,
			Supplier<T> decodeNullTo,
			Function<byte[],T> decoder,
			Comparator<T> comparator,
			T sampleValue){
		super(typeToken, encodeNullTo, encoder, decodeNullTo, decoder, comparator, sampleValue);
	}

	/**
	 * Use if missing a comparator that matches the encoded bytes comparison. The cost is encoding to bytes with the
	 * associated array allocation before each comparison which can happen often if, for example, navigating a large
	 * TreeMap.
	 */
	public ByteArrayFieldCodec(
			TypeToken<T> typeToken,
			Supplier<byte[]> encodeNullTo,
			Function<T,byte[]> encoder,
			Supplier<T> decodeNullTo,
			Function<byte[],T> decoder,
			T sampleValue){
		super(typeToken,
				encodeNullTo,
				encoder,
				decodeNullTo,
				decoder,
				(left, right) -> compareEncoded(encoder, left, right),
				sampleValue);
	}

	private static <T> int compareEncoded(Function<T,byte[]> encoder, T left, T right){
		byte[] leftBytes = encoder.apply(left);
		byte[] rightBytes = encoder.apply(right);
		return Arrays.compareUnsigned(leftBytes, rightBytes);
	}

}
