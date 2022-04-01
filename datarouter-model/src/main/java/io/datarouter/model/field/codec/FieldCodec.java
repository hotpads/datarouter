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
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gson.reflect.TypeToken;

/**
 * Allows a databean field to be different than the database's type.
 *
 * To allow nulls in the database combined with codecs that reject nulls, there are separate fields to specify
 * what to do with nulls.
 */
public class FieldCodec<A,B>{

	private final TypeToken<A> typeToken;
	private final Supplier<B> encodeNullTo;
	private final Function<A,B> encoder;
	private final Supplier<A> decodeNullTo;
	private final Function<B,A> decoder;

	/**
	 * Should compare values equivalently to the underlying storage implementation.
	 */
	private final Comparator<A> comparator;
	private final A sampleValue;

	public FieldCodec(
			TypeToken<A> typeToken,
			Supplier<B> encodeNullTo,
			Function<A,B> encoder,
			Supplier<A> decodeNullTo,
			Function<B,A> decoder,
			Comparator<A> comparator,
			A sampleValue){
		this.typeToken = typeToken;
		this.encodeNullTo = encodeNullTo;
		this.encoder = encoder;
		this.decodeNullTo = decodeNullTo;
		this.decoder = decoder;
		this.comparator = Comparator.nullsFirst(comparator);
		this.sampleValue = sampleValue;
	}

	public TypeToken<A> getTypeToken(){
		return typeToken;
	}

	public B encode(A value){
		return value == null ? encodeNullTo.get() : encoder.apply(value);
	}

	public A decode(B encodedValue){
		return encodedValue == null ? decodeNullTo.get() : decoder.apply(encodedValue);
	}

	public Comparator<A> getComparator(){
		return comparator;
	}

	public A getSampleValue(){
		return this.sampleValue;
	}

}
