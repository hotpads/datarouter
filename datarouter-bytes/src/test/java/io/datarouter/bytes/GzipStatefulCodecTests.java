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

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.codec.bytestringcodec.HexByteStringCodec;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;
import io.datarouter.bytes.compress.gzip.GzipStatefulCodec;
import io.datarouter.scanner.Scanner;

public class GzipStatefulCodecTests{

	@Test
	public void testEncodeDecode(){
		var codec = new GzipStatefulCodec();
		List<String> inputs = Scanner.iterate(0, i -> i + 1)
				.limit(4)
				.map(RawIntCodec.INSTANCE::encode)
				.map(HexByteStringCodec.INSTANCE::encode)
				.list();
		List<byte[]> compressed = Scanner.of(inputs)
				.map(HexByteStringCodec.INSTANCE::decode)
				.map(codec::encode)
				.list();
		List<String> outputs = Scanner.of(compressed)
				.map(codec::decode)
				.map(HexByteStringCodec.INSTANCE::encode)
				.list();
		Assert.assertEquals(outputs, inputs);
	}

}