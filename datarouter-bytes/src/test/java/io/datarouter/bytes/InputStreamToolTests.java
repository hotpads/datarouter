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
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class InputStreamToolTests{

	@Test
	public void testReadNBytes(){
		byte[] input = {0, 1, 2, 3, 4, 5};
		var inputStream = new ByteArrayInputStream(input);
		Assert.assertEquals(InputStreamTool.readNBytes(inputStream, 4), new byte[]{0, 1, 2, 3});
		Assert.assertEquals(InputStreamTool.readNBytes(inputStream, 4), new byte[]{4, 5});
		Assert.assertEquals(InputStreamTool.readNBytes(inputStream, 4), new byte[]{});
	}

	@Test
	public void testScanChunks(){
		byte[] input = {0, 1, 2, 3, 4, 5};
		var inputStream = new ByteArrayInputStream(input);
		List<byte[]> chunks = InputStreamTool.scanChunks(inputStream, 4).list();
		Assert.assertEquals(chunks.get(0), new byte[]{0, 1, 2, 3});
		Assert.assertEquals(chunks.get(1), new byte[]{4, 5});
	}

	@Test
	public void testReadUntilLength(){
		byte[] input = {0, 1, 2, 3, 4};
		var inputStream = new ByteArrayInputStream(input);
		byte[] buffer = new byte[2];

		int numRead1 = InputStreamTool.readUntilLength(inputStream, buffer, 0, buffer.length);
		Assert.assertEquals(numRead1, 2);
		Assert.assertEquals(buffer, new byte[]{0, 1});

		int numRead2 = InputStreamTool.readUntilLength(inputStream, buffer, 0, buffer.length);
		Assert.assertEquals(numRead2, 2);
		Assert.assertEquals(buffer, new byte[]{2, 3});

		int numRead3 = InputStreamTool.readUntilLength(inputStream, buffer, 0, buffer.length);
		Assert.assertEquals(numRead3, 1);
		Assert.assertEquals(buffer, new byte[]{4, 3});//only first byte overwritten

		int numRead4 = InputStreamTool.readUntilLength(inputStream, buffer, 0, buffer.length);
		Assert.assertEquals(numRead4, -1);
		Assert.assertEquals(buffer, new byte[]{4, 3});//nothing overwritten
	}

}
