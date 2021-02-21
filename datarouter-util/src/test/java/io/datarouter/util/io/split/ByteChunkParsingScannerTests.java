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

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.io.split.InputStreamSplitter.ByteChunkParsingScanner;
import io.datarouter.util.io.split.InputStreamSplitter.ByteSplitMapper;

public class ByteChunkParsingScannerTests{

	private static final byte DELIMITER = (byte)'$';
	private static final ByteSplitMapper<String> MAPPER = InputStreamSplitters.TO_STRING_US_ASCII;

	@Test
	public void testByteChunkParsingScannerSingleChunk(){
		List<String> chunkStrings = List.of("a$bb$$ccc$");
		List<String> expected = List.of("a$", "bb$", "$", "ccc$");
		Assert.assertEquals(split(chunkStrings), expected);
	}

	@Test
	public void testByteChunkParsingScannerMultiChunks(){
		List<String> chunkStrings = List.of("a$", "b", "b$", "$", "cc", "c$");
		List<String> expected = List.of("a$", "bb$", "$", "ccc$");
		Assert.assertEquals(split(chunkStrings), expected);
	}

	@Test
	public void testByteChunkParsingScannerMultiChunksWithTrailing(){
		List<String> chunkStrings = List.of("a$", "b", "bb", "b$$cc", "c", "cc");
		List<String> expected = List.of("a$", "bbbb$", "$", "ccccc");
		Assert.assertEquals(split(chunkStrings), expected);
	}

	//allow variable size chunks for more thorough testing
	private static List<String> split(List<String> chunkStrings){
		return Scanner.of(chunkStrings)
				.map(String::getBytes)
				.map(chunk -> InputStreamSplitter.split(chunk, DELIMITER, false, MAPPER))
				.link(chunkTokensScanner -> new ByteChunkParsingScanner<>(chunkTokensScanner, MAPPER))
				.concat(Scanner::of)
				.list();
	}

	@Test
	public void testSkipFirst(){
		List<String> chunkStrings = List.of("aaa$bbb$ccc$");
		boolean skipFirst = true;
		List<String> actual = Scanner.of(chunkStrings)
				.map(String::getBytes)
				.map(chunk -> InputStreamSplitter.split(chunk, DELIMITER, skipFirst, MAPPER))
				.link(chunkTokensScanner -> new ByteChunkParsingScanner<>(chunkTokensScanner, MAPPER))
				.concat(Scanner::of)
				.list();
		List<String> expected = List.of("bbb$", "ccc$");
		Assert.assertEquals(actual, expected);
	}

	@Test(expectedExceptions = {Exception.class})
	public void testSkipFirstFailure(){
		InputStreamSplitter.split(new byte[]{5, 5, 5, 5}, DELIMITER, true, InputStreamSplitters.TO_BYTES);
	}

}