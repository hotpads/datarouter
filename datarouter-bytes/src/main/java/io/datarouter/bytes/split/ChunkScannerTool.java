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
package io.datarouter.bytes.split;

import io.datarouter.scanner.Scanner;

public class ChunkScannerTool{

	public static Scanner<ChunkRange> scanChunks(long fromInclusive, long toExclusive, int chunkSize){
		long totalLength = toExclusive - fromInclusive;
		int initialLength = (int)Math.min(chunkSize, totalLength);
		var intialRange = new ChunkRange(fromInclusive, initialLength);
		return Scanner.iterate(intialRange, previous -> {
					long start = previous.start + previous.length;
					long remainingBytes = toExclusive - start;
					int length = (int)Math.min(chunkSize, remainingBytes);
					return new ChunkRange(start, length);
				})
				.advanceWhile(range -> range.length > 0);
	}

	public static class ChunkRange{

		public final long start;
		public final int length;

		public ChunkRange(long start, int length){
			this.start = start;
			this.length = length;
		}

		@Override
		public String toString(){
			return String.format("start=%s, length=%s", start, length);
		}

	}

}
