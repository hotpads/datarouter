/**
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
package io.datarouter.util.io.split;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import io.datarouter.scanner.BaseLinkedScanner;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.array.PagedObjectArray;
import io.datarouter.util.bytes.ByteTool;

public class InputStreamSplitter<T>{

	private final byte delimiter;
	private final boolean skipFirst;
	private final ByteSplitMapper<T> mapper;

	public InputStreamSplitter(
			byte delimiter,
			boolean skipFirst,
			ByteSplitMapper<T> mapper){
		this.delimiter = delimiter;
		this.skipFirst = skipFirst;
		this.mapper = mapper;
	}

	public Scanner<List<T>> split(Scanner<byte[]> byteArrays, ExecutorService exec, int numThreads){
		var remainingSkipFirst = new AtomicBoolean(skipFirst);
		return byteArrays
				.parallel(new ParallelScannerContext(exec, numThreads, false))
				.map(chunk -> split(chunk, delimiter, remainingSkipFirst.getAndSet(false), mapper))
				.link(chunkTokensScanner -> new ByteChunkParsingScanner<>(chunkTokensScanner, mapper));
	}

	public interface ByteSplitMapper<T>{

		T apply(byte[] bytes, int start, int length);

	}

	/**
	 * @param delimiter  Will be returned as the trailing byte of each token.
	 */
	public static <T> ParsedByteChunk<T> split(
			byte[] chunk,
			byte delimiter,
			boolean skipFirst,
			ByteSplitMapper<T> mapper){
		boolean pendingSkip = skipFirst;
		byte[] first = null;
		PagedObjectArray<T> middle = new PagedObjectArray<>(16);
		int count = 0;
		int start = 0;
		for(int i = 0; i < chunk.length; ++i){
			if(chunk[i] == delimiter){
				if(pendingSkip && count == 0){
					pendingSkip = false;
				}else{
					int len = i - start + 1;
					if(count == 0){
						first = ByteTool.copyOfRange(chunk, start, len);
					}else{
						T mapped = mapper.apply(chunk, start, len);
						middle.add(mapped);
					}
					++count;
				}
				start = i + 1;
			}
		}
		int lastTokenLength = chunk.length - start;
		byte[] last = null;
		if(lastTokenLength > 0){
			byte[] lastToken = ByteTool.copyOfRange(chunk, start, lastTokenLength);
			if(lastToken[lastToken.length - 1] == delimiter){
				T mapped = mapper.apply(lastToken, start, lastTokenLength);
				middle.add(mapped);
			}else{
				last = lastToken;
			}
		}
		if(pendingSkip && count == 0){
			throw new RuntimeException("Couldn't skip first token as delimiter not found in first chunk.");
		}
		return new ParsedByteChunk<>(first, middle, last);
	}

	public static class ParsedByteChunk<T>{

		public final byte[] first;// potential continuation of previous token
		public final PagedObjectArray<T> middle;// complete tokens
		public final byte[] last;// potential prefix of later token

		public ParsedByteChunk(byte[] first, PagedObjectArray<T> middle, byte[] last){
			if(first == null && middle.size() == 0 && last == null){
				throw new IllegalArgumentException("no data");
			}
			this.first = first;
			this.middle = middle;
			this.last = last;
		}

		public int totalTokens(){
			int total = 0;
			if(first != null){
				++total;
			}
			total += middle.size();
			if(last != null){
				++total;
			}
			return total;
		}

		@Override
		public String toString(){
			var sb = new StringBuilder();
			if(first != null){
				sb.append("f" + Arrays.toString(first));
			}
			middle.forEach(token -> sb.append("m" + token));
			if(last != null){
				sb.append("l" + Arrays.toString(last));
			}
			return sb.toString();
		}

	}

	public static class ByteChunkParsingScanner<T> extends BaseLinkedScanner<ParsedByteChunk<T>,List<T>>{

		private final ByteSplitMapper<T> mapper;
		private final PendingChunk<T> pending;
		private byte[] carryover;

		public ByteChunkParsingScanner(Scanner<ParsedByteChunk<T>> chunks, ByteSplitMapper<T> mapper){
			super(chunks);
			this.mapper = mapper;
			pending = new PendingChunk<>();
			carryover = null;
		}

		@Override
		public boolean advanceInternal(){
			while(true){
				if(carryover != null){
					if(pending.hasFirst()){
						carryover = ByteTool.concatenate(carryover, pending.takeFirst());
						T mappedCarryover = mapper.apply(carryover, 0, carryover.length);
						current = Collections.singletonList(mappedCarryover);
						carryover = null;
						return true;
					}
					if(pending.hasLast()){
						carryover = ByteTool.concatenate(carryover, pending.takeLast());
					}
				}
				if(pending.hasFirst()){
					byte[] first = pending.takeFirst();
					T mappedFirst = mapper.apply(first, 0, first.length);
					current = Collections.singletonList(mappedFirst);
					return true;
				}
				if(pending.hasMiddle()){
					current = pending.takeMiddle();
					return true;
				}
				if(pending.hasLast()){
					carryover = pending.takeLast();
				}
				if(input.advance()){
					pending.reload(input.current());
					continue;
				}
				break;
			}
			if(carryover != null){
				T mappedCarryover = mapper.apply(carryover, 0, carryover.length);
				current = Collections.singletonList(mappedCarryover);
				carryover = null;
				return true;
			}
			return false;
		}

	}

	private static class PendingChunk<T>{

		byte[] first;
		List<T> middle;
		byte[] last;

		void reload(ParsedByteChunk<T> tokens){
			if(hasFirst()){
				throw new IllegalStateException("first still exists");
			}
			if(hasMiddle()){
				throw new IllegalStateException("middle still exists");
			}
			if(hasLast()){
				throw new IllegalStateException("last still exists");
			}
			first = tokens.first;
			middle = tokens.middle.isEmpty() ? null : tokens.middle;
			last = tokens.last;
		}

		boolean hasFirst(){
			return first != null;
		}

		boolean hasMiddle(){
			return middle != null;
		}

		boolean hasLast(){
			return last != null;
		}

		byte[] takeFirst(){
			if(first == null){
				throw new IllegalStateException("first is missing");
			}
			var result = first;
			first = null;
			return result;
		}

		List<T> takeMiddle(){
			if(middle == null){
				throw new IllegalStateException("middle is missing");
			}
			var result = middle;
			middle = null;
			return result;
		}

		byte[] takeLast(){
			if(last == null){
				throw new IllegalStateException("last is missing");
			}
			var result = last;
			last = null;
			return result;
		}

	}

}