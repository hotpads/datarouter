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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

import io.datarouter.bytes.Codec.NullPassthroughCodec;
import io.datarouter.enums.CaseInsensitiveStringMappedEnum;
import io.datarouter.enums.MappedEnum;
import io.datarouter.enums.StringMappedEnum;

public class StringMappedEnumFieldCodec<E> extends FieldCodec<E,String>{
	private static final Logger logger = LoggerFactory.getLogger(StringMappedEnumFieldCodec.class);

	public StringMappedEnumFieldCodec(MappedEnum<E,String> mappedEnum){
		super(TypeToken.get(mappedEnum.getEnumClass()),
				NullPassthroughCodec.of(mappedEnum::toKey, mappedEnum::fromOrThrow),
				Comparator.comparing(mappedEnum::toKey),
				mappedEnum.getSampleValue(),
				makeDocString(mappedEnum));
		logCaseInsensitive(mappedEnum);
	}

	private static <E> String makeDocString(MappedEnum<E,String> mappedEnum){
		return mappedEnum.getValueByKey().keySet().stream()
				.collect(Collectors.joining(", ", "[", "]"));
	}

	private static void logCaseInsensitive(MappedEnum<?,?> mappedEnum){
		if(mappedEnum instanceof CaseInsensitiveStringMappedEnum){
			logger.warn(
					"Databean Fielder: Please replace {} with a case-sensitive {} for enumClass={}",
					CaseInsensitiveStringMappedEnum.class.getSimpleName(),
					StringMappedEnum.class.getSimpleName(),
					mappedEnum.getEnumClass(),
					new Exception());
		}
	}

}
