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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.reflect.TypeToken;

import io.datarouter.bytes.Codec;
import io.datarouter.scanner.Scanner;

public class LongListToCsvFieldCodec extends FieldCodec<List<Long>,String>{

	private static final String COMMA = ",";

	public LongListToCsvFieldCodec(){
		super(new TypeToken<List<Long>>(){},
				Codec.of(
						list -> Optional.ofNullable(list)
								.map(LongListToCsvFieldCodec::encodeList)
								.orElse(null),
						csv -> Optional.ofNullable(csv)
								.map(LongListToCsvFieldCodec::decodeCsv)
								.orElseGet(() -> new ArrayList<>(0))),
				Comparator.nullsFirst(LongListToCsvFieldCodec::compare),
				List.of(),
				null);
	}

	private static String encodeList(List<Long> list){
		return list.stream()
				.map(Number::toString)
				.collect(Collectors.joining(COMMA));
	}

	private static List<Long> decodeCsv(String csv){
		if(csv.isEmpty()){
			return new ArrayList<>(0);
		}
		return Scanner.of(csv.split(COMMA))
				.map(Long::valueOf)
				.collect(ArrayList::new);// mutable so databeans can modify
	}

	private static int compare(List<Long> first, List<Long> second){
		String firstCsv = encodeList(first);
		String secondCsv = encodeList(second);
		return firstCsv.compareTo(secondCsv);
	}

}
