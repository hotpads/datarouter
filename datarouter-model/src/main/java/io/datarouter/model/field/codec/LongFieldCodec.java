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

public class LongFieldCodec<T> extends FieldCodec<T,Long>{

	public LongFieldCodec(
			TypeToken<T> typeToken,
			Supplier<Long> encodeNullTo,
			Function<T,Long> encoder,
			Supplier<T> decodeNullTo,
			Function<Long,T> decoder,
			Comparator<T> comparator,
			T sampleValue){
		super(typeToken, encodeNullTo, encoder, decodeNullTo, decoder, comparator, sampleValue);
	}

}
