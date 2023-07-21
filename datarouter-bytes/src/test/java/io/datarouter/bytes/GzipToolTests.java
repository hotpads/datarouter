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
package io.datarouter.bytes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.codec.bytestringcodec.HexByteStringCodec;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.bytes.compress.gzip.GzipTool;
import io.datarouter.bytes.io.InputStreamTool;
import io.datarouter.scanner.Scanner;

public class GzipToolTests{
	private static final Logger logger = LoggerFactory.getLogger(GzipToolTests.class);

	@Test
	public void testEncodeDecode(){
		String input = "0123456789";
		byte[] inputBytes = StringCodec.UTF_8.encode(input);
		byte[] compressedBytes = GzipTool.encode(inputBytes);
//		HexBlockTool.print(compressedData);
		String hex = "1f8b08000000000000ff3330343236313533b7b00400c6c784a60a000000";
		byte[] hexBytes = HexBlockTool.fromHexBlock(hex);
		Assert.assertEquals(compressedBytes, hexBytes);
		byte[] decompressedBytes = GzipTool.decode(compressedBytes);
		Assert.assertEquals(decompressedBytes, inputBytes);
		String output = StringCodec.UTF_8.decode(decompressedBytes);
		Assert.assertEquals(output, input);
	}

	@Test
	public void testIndividualValues(){
		var toBytes = StringCodec.UTF_8;
		var toGzip = GzipTool.CODEC;
		var toHex = HexByteStringCodec.INSTANCE;
		Codec<String,String> toBytesToGzipToHex = Codec.of(
				input -> Optional.of(input)
						.map(toBytes::encode)
						.map(toGzip::encode)
						.map(toHex::encode)
						.orElseThrow(),
				hex -> Optional.of(hex)
						.map(toHex::decode)
						.map(toGzip::decode)
						.map(toBytes::decode)
						.orElseThrow());
		List<String> values = List.of("Dolphins", "are", "mammals");
		List<String> encodedValues = Scanner.of(values)
				.map(toBytesToGzipToHex::encode)
				.list();
		encodedValues.forEach(encodedValue -> logger.info("encoded={}", encodedValue));
		List<String> decodedValues = Scanner.of(encodedValues)
				.map(toBytesToGzipToHex::decode)
				.list();
		decodedValues.forEach(decodedValue -> logger.info("decoded={}", decodedValue));
		Assert.assertEquals(decodedValues, values);
	}

	@Test
	public void testEncodeInputStream(){
		byte[] input = Scanner.iterate(0L, i -> i + 1)
				.limit(100_000)
				.map(i -> Long.toString(i))
				.map(StringCodec.US_ASCII::encode)
				.listTo(ByteTool::concat);
		byte[] expectedOutput = GzipTool.encode(input);
		InputStream gzippedInputStream = GzipTool.encodeToInputStream(new ByteArrayInputStream(input));
		byte[] actualOutput = InputStreamTool.toArray(gzippedInputStream);
		Assert.assertEquals(actualOutput, expectedOutput);
	}

}
