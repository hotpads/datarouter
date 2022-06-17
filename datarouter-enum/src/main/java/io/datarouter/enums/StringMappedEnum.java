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
package io.datarouter.enums;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MappedEnum specific to Strings, including length validation.
 */
public class StringMappedEnum<E>
extends MappedEnum<E,String>{

	private final int maxLength;

	public StringMappedEnum(E[] values, Function<E,String> keyExtractor){
		this(values, keyExtractor, Integer.MAX_VALUE);
	}

	public StringMappedEnum(E[] values, Function<E,String> keyExtractor, int maxLength){
		super(values, keyExtractor, String::toLowerCase);
		this.maxLength = maxLength;
		validateLengths(getValueByKey().keySet(), maxLength);
	}

	public int maxLength(){
		return maxLength;
	}

	private static final void validateLengths(Collection<String> strings, int maxLength){
		List<String> tooLong = strings.stream()
				.filter(str -> str.length() > maxLength)
				.collect(Collectors.toList());
		if(!tooLong.isEmpty()){
			String message = String.format("Some entries (%s) exceed the max length=%s", tooLong, maxLength);
			throw new IllegalArgumentException(message);
		}
	}

}
