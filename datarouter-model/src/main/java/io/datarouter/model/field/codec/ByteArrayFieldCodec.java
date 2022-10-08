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
import java.util.Objects;
import java.util.function.Function;

import com.google.gson.reflect.TypeToken;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.Codec.NullPassthroughCodec;
import io.datarouter.bytes.EmptyArray;

public class ByteArrayFieldCodec<T> extends FieldCodec<T,byte[]>{

	public static final ByteArrayFieldCodec<byte[]> IDENTITY = new ByteArrayFieldCodec<>(
			new TypeToken<byte[]>(){},
			NullPassthroughCodec.of(Function.identity(), Function.identity()),
			Arrays::compareUnsigned,
			EmptyArray.BYTE,
			null);

	private ByteArrayFieldCodec(
			TypeToken<T> typeToken,
			Codec<T,byte[]> codec,
			Comparator<T> comparator,
			T sampleValue,
			String docString){
		super(typeToken, codec, comparator, sampleValue, docString);
	}

	public static class ByteArrayFieldCodecBuilder<T>{

		private TypeToken<T> typeToken;
		private Codec<T,byte[]> codec;
		private Comparator<T> comparator;
		private T sampleValue;
		private String docString;

		public ByteArrayFieldCodecBuilder(
				TypeToken<T> typeToken,
				Codec<T,byte[]> codec){
			this.typeToken = typeToken;
			this.codec = codec;
			this.comparator = (left, right) -> compareEncoded(codec, left, right);
		}

		public ByteArrayFieldCodec<T> build(){
			return new ByteArrayFieldCodec<>(
					typeToken,
					codec,
					comparator,
					sampleValue,
					docString);

		}

		/**
		 * Use if you have a comparator that matches the encoded bytes comparison but is more efficient than encoding
		 * to bytes with associated allocations for each comparison.
		 */
		public ByteArrayFieldCodecBuilder<T> setComparator(Comparator<T> comparator){
			Objects.requireNonNull(comparator, "Please provide a Comparator");
			this.comparator = comparator;
			return this;
		}

		public ByteArrayFieldCodecBuilder<T> setSampleValue(T sampleValue){
			this.sampleValue = sampleValue;
			return this;
		}

		public ByteArrayFieldCodecBuilder<T> setDocString(String docString){
			this.docString = docString;
			return this;
		}

	}

	/**
	 * Default comparator encodes both values to bytes and compares unsigned
	 */
	private static <T> int compareEncoded(Codec<T,byte[]> codec, T left, T right){
		byte[] leftBytes = codec.encode(left);
		byte[] rightBytes = codec.encode(right);
		return Arrays.compareUnsigned(leftBytes, rightBytes);
	}

}
