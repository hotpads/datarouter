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
package io.datarouter.util.split.collect;

import java.util.Arrays;
import java.util.List;

import io.datarouter.util.array.PagedObjectArray;
import io.datarouter.util.split.ByteChunkSplitterCollector;

public class ByteArrayByteChunkCollector implements ByteChunkSplitterCollector<byte[]>{

	private final PagedObjectArray<byte[]> results = new PagedObjectArray<>(64);

	@Override
	public byte[] encode(byte[] bytes, int start, int length){
		int to = start + length;
		return Arrays.copyOfRange(bytes, start, to);
	}

	@Override
	public void collect(byte[] bytes, int start, int length){
		results.add(encode(bytes, start, length));
	}

	@Override
	public List<byte[]> toList(){
		return results;
	}

}
