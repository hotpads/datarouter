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

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.codec.intcodec.RawIntCodec;

public class ByteToolTests{

	private static final RawIntCodec RAW_INT_CODEC = RawIntCodec.INSTANCE;

	@Test
	public void testUnsignedIncrement(){
		byte[] bytesA = RAW_INT_CODEC.encode(0);
		int a2 = RAW_INT_CODEC.decode(ByteTool.unsignedIncrement(bytesA), 0);
		Assert.assertTrue(a2 == 1);

		byte[] bytesB = RAW_INT_CODEC.encode(-1);
		byte[] actuals = ByteTool.unsignedIncrement(bytesB);
		byte[] expected = {1, 0, 0, 0, 0};
		Assert.assertEquals(actuals, expected);

		byte[] bytesC = RAW_INT_CODEC.encode(255);// should wrap to the next significant byte
		int c2 = RAW_INT_CODEC.decode(ByteTool.unsignedIncrement(bytesC), 0);
		Assert.assertTrue(c2 == 256);
	}

	@Test
	public void testUnsignedIncrementOverflowToNull(){
		byte[] bytesA = RAW_INT_CODEC.encode(0);
		int a2 = RAW_INT_CODEC.decode(ByteTool.unsignedIncrementOverflowToNull(bytesA), 0);
		Assert.assertTrue(a2 == 1);

		byte[] bytesB = RAW_INT_CODEC.encode(-1);
		byte[] b2 = ByteTool.unsignedIncrementOverflowToNull(bytesB);
		Assert.assertTrue(b2 == null);

		byte[] bytesC = RAW_INT_CODEC.encode(255);// should wrap to the next significant byte
		int c2 = RAW_INT_CODEC.decode(ByteTool.unsignedIncrementOverflowToNull(bytesC), 0);
		Assert.assertTrue(c2 == 256);
	}

	@Test
	public void testPadPrefix(){
		Assert.assertEquals(ByteTool.padPrefix(new byte[]{55, -21}, 7), new byte[]{0, 0, 0, 0, 0, 55, -21});
	}

	@Test
	public void testTotalLength(){
		byte[][] arrays = {"a".getBytes(), "bb".getBytes()};
		Assert.assertEquals(ByteTool.totalLength(arrays), 3);
		Assert.assertEquals(ByteTool.totalLength(Arrays.asList(arrays)), 3);
	}

	@Test
	public void testConcat(){
		byte[][] arrays = {"a".getBytes(), "bb".getBytes()};
		Assert.assertEquals(new String(ByteTool.concat(arrays)), "abb");
		Assert.assertEquals(new String(ByteTool.concat(Arrays.asList(arrays))), "abb");
	}

}
