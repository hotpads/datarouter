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
package io.datarouter.bytes.split.collect;

import java.util.List;

import io.datarouter.bytes.Ascii;
import io.datarouter.bytes.split.ByteChunkSplitterCollector;
import io.datarouter.scanner.PagedList;

public class CrlfStringByteChunkCollector implements ByteChunkSplitterCollector<String>{

	public static final byte DELIMITER = Ascii.LINE_FEED.code;

	private final PagedList<String> results = new PagedList<>(64);

	@Override
	public String encode(byte[] bytes, int start, int length){
		return new String(bytes, start, length - 2);// -2 for CR and LF
	}

	@Override
	public void collect(byte[] bytes, int start, int length){
		results.add(encode(bytes, start, length));
	}

	@Override
	public List<String> toList(){
		return results;
	}

}
