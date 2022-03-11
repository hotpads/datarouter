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

public class FieldCodec<A,B>{

	private final Class<A> valueClass;
	private final Function<A,B> encoder;
	private final Function<B,A> decoder;

	/**
	 * Should compare values equivalently to the underlying storage implementation.
	 */
	private final Comparator<A> comparator;

	public FieldCodec(
			Class<A> valueClass,
			Function<A,B> encoder,
			Function<B,A> decoder,
			Comparator<A> comparator){
		this.valueClass = valueClass;
		this.encoder = encoder;
		this.decoder = decoder;
		this.comparator = Comparator.nullsFirst(comparator);
	}

	public Class<A> getValueClass(){
		return valueClass;
	}

	public B encode(A value){
		return value == null ? null : encoder.apply(value);
	}

	public A decode(B encodedValue){
		return encodedValue == null ? null : decoder.apply(encodedValue);
	}

	public Comparator<A> getComparator(){
		return comparator;
	}

}
