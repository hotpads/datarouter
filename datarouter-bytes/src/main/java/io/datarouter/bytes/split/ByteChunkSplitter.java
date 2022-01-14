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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import io.datarouter.bytes.ByteTool;
import io.datarouter.scanner.BaseLinkedScanner;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;

public class ByteChunkSplitter<T>{

	private final Supplier<ByteChunkSplitterCollector<T>> collectorSupplier;

	public ByteChunkSplitter(Supplier<ByteChunkSplitterCollector<T>> collectorSupplier){
		this.collectorSupplier = collectorSupplier;
	}

	public Scanner<List<T>> split(
			Scanner<byte[]> byteArrays,
			ExecutorService exec,
			int numThreads,
			byte delimiter,
			boolean skipFirst){
		AtomicBoolean remainingSkipFirst = new AtomicBoolean(skipFirst);
		return byteArrays
				.parallel(new ParallelScannerContext(exec, numThreads, false))
				.map(chunk -> split(chunk, delimiter, remainingSkipFirst.getAndSet(false), collectorSupplier.get()))
				.link(chunkTokensScanner -> new ByteChunkParsingScanner<>(chunkTokensScanner, collectorSupplier.get()));
	}

	/**
	 * @param delimiter  Will be returned as the trailing byte of each token.
	 */
	public static <T> ParsedByteChunk<T> split(
			byte[] chunk,
			byte delimiter,
			boolean skipFirst,
			ByteChunkSplitterCollector<T> collector){
		boolean pendingSkip = skipFirst;
		byte[] first = null;
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
						collector.collect(chunk, start, len);
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
				collector.collect(lastToken, start, lastTokenLength);
			}else{
				last = lastToken;
			}
		}
		if(pendingSkip && count == 0){
			throw new RuntimeException("Couldn't skip first token as delimiter not found in first chunk.");
		}
		return new ParsedByteChunk<>(first, collector.toList(), last);
	}

	public static class ParsedByteChunk<T>{

		public final byte[] first;// potential continuation of previous token
		public final List<T> middle;// complete tokens
		public final byte[] last;// potential prefix of later token

		public ParsedByteChunk(byte[] first, List<T> middle, byte[] last){
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
			StringBuilder sb = new StringBuilder();
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

		private final ByteChunkSplitterCollector<T> collector;
		private final PendingChunk<T> pending;
		private byte[] carryover;

		public ByteChunkParsingScanner(Scanner<ParsedByteChunk<T>> chunks, ByteChunkSplitterCollector<T> collector){
			super(chunks);
			this.collector = collector;
			pending = new PendingChunk<>();
			carryover = null;
		}

		@Override
		public boolean advanceInternal(){
			while(true){
				if(carryover != null){
					if(pending.hasFirst()){
						carryover = ByteTool.concatenate2(carryover, pending.takeFirst());
						T mappedCarryover = collector.encode(carryover, 0, carryover.length);
						current = Collections.singletonList(mappedCarryover);
						carryover = null;
						return true;
					}
					if(pending.hasLast()){
						carryover = ByteTool.concatenate2(carryover, pending.takeLast());
					}
				}
				if(pending.hasFirst()){
					byte[] first = pending.takeFirst();
					T mappedFirst = collector.encode(first, 0, first.length);
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
				T mappedCarryover = collector.encode(carryover, 0, carryover.length);
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
			byte[] result = first;
			first = null;
			return result;
		}

		List<T> takeMiddle(){
			if(middle == null){
				throw new IllegalStateException("middle is missing");
			}
			List<T> result = middle;
			middle = null;
			return result;
		}

		byte[] takeLast(){
			if(last == null){
				throw new IllegalStateException("last is missing");
			}
			byte[] result = last;
			last = null;
			return result;
		}

	}

}
