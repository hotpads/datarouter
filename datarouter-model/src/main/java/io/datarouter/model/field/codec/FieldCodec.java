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

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;

import com.google.gson.reflect.TypeToken;

import io.datarouter.bytes.Codec;

/**
 * Allows a databean field to be different than the database's type.
 */
public class FieldCodec<A,B>{

	private final TypeToken<A> typeToken;
	private final Codec<A,B> codec;

	/**
	 * Should compare values equivalently to the underlying storage implementation.
	 */
	private final Comparator<A> comparator;
	private final A sampleValue;
	private final String docString;

	public FieldCodec(
			TypeToken<A> typeToken,
			Codec<A,B> codec,
			Comparator<A> comparator,
			A sampleValue,
			String docString){
		this.typeToken = typeToken;
		this.codec = codec;
		this.comparator = Comparator.nullsFirst(comparator);
		this.sampleValue = sampleValue;
		this.docString = docString;
	}

	public TypeToken<A> getTypeToken(){
		return typeToken;
	}

	public B encode(A value){
		return codec.encode(value);
	}

	public A decode(B encodedValue){
		return codec.decode(encodedValue);
	}

	public Comparator<A> getComparator(){
		return comparator;
	}

	public A getSampleValue(){
		return sampleValue;
	}

	public Optional<String> findDocString(){
		return Optional.ofNullable(docString);
	}

	public Optional<String> findAuxiliaryHumanReadableString(
			@SuppressWarnings("unused")
			A object,
			@SuppressWarnings("unused")
			DateTimeFormatter dateTimeFormatter,
			@SuppressWarnings("unused")
			ZoneId zoneId){
		return Optional.empty();
	}

}
