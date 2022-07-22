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
import java.util.stream.Collectors;

import com.google.gson.reflect.TypeToken;

import io.datarouter.bytes.Codec.NullPassthroughCodec;
import io.datarouter.enums.MappedEnum;

public class IntegerMappedEnumFieldCodec<E> extends FieldCodec<E,Integer>{

	public IntegerMappedEnumFieldCodec(MappedEnum<E,Integer> mappedEnum){
		super(TypeToken.get(mappedEnum.getEnumClass()),
				NullPassthroughCodec.of(mappedEnum::toKey, mappedEnum::fromOrThrow),
				Comparator.comparing(mappedEnum::toKey),
				mappedEnum.getSampleValue(),
				makeDocString(mappedEnum));
	}

	private static <E> String makeDocString(MappedEnum<E,Integer> mappedEnum){
		return mappedEnum.getValueByKey().entrySet().stream()
				.map(entry -> entry.getKey() + ": " + entry.getValue())
				.collect(Collectors.joining(", ", "[", "]"));
	}

}
