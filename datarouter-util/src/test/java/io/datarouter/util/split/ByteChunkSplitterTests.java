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
package io.datarouter.util.split;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.Java9;
import io.datarouter.util.split.ByteChunkSplitter.ByteChunkParsingScanner;

public class ByteChunkSplitterTests{

	private static final byte DELIMITER = '$';

	private static class Collector implements ByteChunkSplitterCollector<String>{

		private final List<String> result = new ArrayList<>();

		@Override
		public String encode(byte[] bytes, int start, int length){
			return new String(bytes, start, length, StandardCharsets.US_ASCII);
		}

		@Override
		public void collect(byte[] bytes, int start, int length){
			result.add(encode(bytes, start, length));
		}

		@Override
		public List<String> toList(){
			return result;
		}

	}

	@Test
	public void testByteChunkParsingScannerSingleChunk(){
		List<String> chunkStrings = Java9.listOf("a$bb$$ccc$");
		List<String> expected = Java9.listOf("a$", "bb$", "$", "ccc$");
		Assert.assertEquals(split(chunkStrings), expected);
	}

	@Test
	public void testByteChunkParsingScannerMultiChunks(){
		List<String> chunkStrings = Java9.listOf("a$", "b", "b$", "$", "cc", "c$");
		List<String> expected = Java9.listOf("a$", "bb$", "$", "ccc$");
		Assert.assertEquals(split(chunkStrings), expected);
	}

	@Test
	public void testByteChunkParsingScannerMultiChunksWithTrailing(){
		List<String> chunkStrings = Java9.listOf("a$", "b", "bb", "b$$cc", "c", "cc");
		List<String> expected = Java9.listOf("a$", "bbbb$", "$", "ccccc");
		Assert.assertEquals(split(chunkStrings), expected);
	}

	//allow variable size chunks for more thorough testing
	private static List<String> split(List<String> chunkStrings){
		return Scanner.of(chunkStrings)
				.map(String::getBytes)
				.map(chunk -> ByteChunkSplitter.split(chunk, DELIMITER, false, new Collector()))
				.link(chunkTokensScanner -> new ByteChunkParsingScanner<>(chunkTokensScanner, new Collector()))
				.concat(Scanner::of)
				.list();
	}

	@Test
	public void testSkipFirst(){
		List<String> chunkStrings = Java9.listOf("aaa$bbb$ccc$");
		List<String> actual = Scanner.of(chunkStrings)
				.map(String::getBytes)
				.map(chunk -> ByteChunkSplitter.split(chunk, DELIMITER, true, new Collector()))
				.link(chunkTokensScanner -> new ByteChunkParsingScanner<>(chunkTokensScanner, new Collector()))
				.concat(Scanner::of)
				.list();
		List<String> expected = Java9.listOf("bbb$", "ccc$");
		Assert.assertEquals(actual, expected);
	}

	@Test(expectedExceptions = {Exception.class})
	public void testSkipFirstFailure(){
		ByteChunkSplitter.split(new byte[]{5, 5, 5, 5}, DELIMITER, true, new Collector());
	}

}