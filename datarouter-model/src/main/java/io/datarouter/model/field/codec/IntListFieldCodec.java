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

import java.util.List;

import com.google.gson.reflect.TypeToken;

import io.datarouter.bytes.Codec.NullPassthroughCodec;
import io.datarouter.bytes.codec.list.intlist.IntListCodec;
import io.datarouter.model.field.codec.ByteArrayFieldCodec.ByteArrayFieldCodecBuilder;
import io.datarouter.util.collection.ListTool;

public class IntListFieldCodec{

	public static final ByteArrayFieldCodec<List<Integer>> INSTANCE = new ByteArrayFieldCodecBuilder<>(
			new TypeToken<List<Integer>>(){},
			NullPassthroughCodec.of(IntListCodec.INSTANCE::encode, IntListCodec.INSTANCE::decode))
			.setComparator(ListTool::compare)
			.build();

}
