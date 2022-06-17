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

import com.google.gson.reflect.TypeToken;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.codec.stringcodec.StringCodec;

public class StringListToDelimitedBytesFieldCodec extends FieldCodec<List<String>,byte[]>{

	public StringListToDelimitedBytesFieldCodec(String delimiter){
		super(new TypeToken<List<String>>(){},
			Codec.of(
					memory -> Optional.ofNullable(memory)
							.map(memory2 -> StringListToDelimitedStringFieldCodec.encode(memory2, delimiter))
							.map(StringCodec.UTF_8::encode)
							.orElse(null),
					stored -> Optional.ofNullable(stored)
							.map(StringCodec.UTF_8::decode)
							.map(stored2 -> StringListToDelimitedStringFieldCodec.decode(stored2, delimiter))
							.orElseGet(() -> new ArrayList<>(0))),
			Comparator.nullsFirst((a, b) -> StringListToDelimitedStringFieldCodec.compare(a, b, delimiter)),
			List.of(),
			null);
	}

}
