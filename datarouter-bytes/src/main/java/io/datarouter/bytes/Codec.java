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
package io.datarouter.bytes;

import java.util.function.Function;

import io.datarouter.bytes.ReplacingFunction.NullPassthroughFunction;

/**
 * A bi-directional Function where encoding then decoding should typically return the original value.
 */
public interface Codec<A,B>{

	B encode(A value);
	A decode(B encodedValue);

	/**
	 * Useful for testing
	 */
	default A encodeAndDecode(A value){
		return decode(encode(value));
	}

	public static <A,B> Codec<A,B> of(
			Function<A,B> encodeFunction,
			Function<B,A> decodeFunction){
		return new FunctionalCodec<>(encodeFunction, decodeFunction);
	}

	/**
	 * Build a Codec from two Functions.
	 */
	public static class FunctionalCodec<A,B>
	implements Codec<A,B>{

		private final Function<A,B> encodeFunction;
		private final Function<B,A> decodeFunction;

		public FunctionalCodec(
				Function<A,B> encodeFunction,
				Function<B,A> decodeFunction){
			this.encodeFunction = encodeFunction;
			this.decodeFunction = decodeFunction;
		}

		@Override
		public B encode(A value){
			return encodeFunction.apply(value);
		}

		@Override
		public A decode(B encodedValue){
			return decodeFunction.apply(encodedValue);
		}

	}


	/**
	 * Skips the inner codecs when provided values are null, directly returning null.
	 */
	public static class NullPassthroughCodec<A,B>
	extends FunctionalCodec<A,B>{

		public NullPassthroughCodec(
				Function<A,B> encodeFunction,
				Function<B,A> decodeFunction){
			super(NullPassthroughFunction.of(encodeFunction),
					NullPassthroughFunction.of(decodeFunction));
		}

		public static <A,B> NullPassthroughCodec<A,B> of(
				Function<A,B> encodeFunction,
				Function<B,A> decodeFunction){
			return new NullPassthroughCodec<>(encodeFunction, decodeFunction);
		}

	}

}
