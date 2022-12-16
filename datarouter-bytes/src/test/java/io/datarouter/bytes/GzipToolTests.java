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

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.scanner.Scanner;

public class GzipToolTests{

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
