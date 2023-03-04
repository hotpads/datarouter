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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.google.gson.reflect.TypeToken;

import io.datarouter.bytes.Codec;

public class StringListToDelimitedStringFieldCodec extends FieldCodec<List<String>,String>{

	private static final String EMPTY_STRING = "";

	public StringListToDelimitedStringFieldCodec(String delimiter){
		super(new TypeToken<List<String>>(){},
				Codec.of(
						memory -> Optional.ofNullable(memory)
								.map(memory2 -> encode(memory2, delimiter))
								.orElse(null),
						stored -> Optional.ofNullable(stored)
								.map(stored2 -> decode(stored2, delimiter))
								.orElseGet(() -> new ArrayList<>(0))),
				Comparator.nullsFirst((a, b) -> compare(a, b, delimiter)),
				List.of(),
				null);
	}

	public static String encode(List<String> strings, String delimiter){
		if(strings.size() == 1 && EMPTY_STRING.equals(strings.get(0))){
			throw new IllegalArgumentException("Encoding is ambiguous for a single empty string.");
		}
		strings.forEach(string -> {
			if(string.contains(delimiter)){
				String message = String.format("Strings cannot contain the delimiter=%s.", delimiter);
				throw new IllegalArgumentException(message);
			}
		});
		return String.join(delimiter, strings);
	}

	public static List<String> decode(String string, String delimiter){
		if("".equals(string)){
			return new ArrayList<>(0);
		}
		return new ArrayList<>(Arrays.asList(string.split(Pattern.quote(delimiter))));
	}

	public static int compare(List<String> first, List<String> second, String delimiter){
		String firstCsv = encode(first, delimiter);
		String secondCsv = encode(second, delimiter);
		return firstCsv.compareTo(secondCsv);
	}

}
