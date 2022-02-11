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
package io.datarouter.bytes.codec.intcodec;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.VarIntTool;

public class VarIntToolTests{

	private static final byte[] LONG_MAX_VALUE_BYTES = {-1, -1, -1, -1, -1, -1, -1, -1, 127};
	private static final byte[] INT_MAX_VALUE_BYTES = {-1, -1, -1, -1, 7};

	@Test
	public void testNumBytes(){
		Assert.assertEquals(VarIntTool.length(0), 1);
		Assert.assertEquals(VarIntTool.length(1), 1);
		Assert.assertEquals(VarIntTool.length(100), 1);
		Assert.assertEquals(VarIntTool.length(126), 1);
		Assert.assertEquals(VarIntTool.length(127), 1);
		Assert.assertEquals(VarIntTool.length(128), 2);
		Assert.assertEquals(VarIntTool.length(129), 2);
		Assert.assertEquals(VarIntTool.length(Integer.MAX_VALUE), 5);
		Assert.assertEquals(VarIntTool.length(Long.MAX_VALUE), 9);
	}

	@Test
	public void testToBytes(){
		Assert.assertEquals(VarIntTool.encode(0), new byte[]{0});
		Assert.assertEquals(VarIntTool.encode(1), new byte[]{1});
		Assert.assertEquals(VarIntTool.encode(63), new byte[]{63});
		Assert.assertEquals(VarIntTool.encode(127), new byte[]{127});
		Assert.assertEquals(VarIntTool.encode(128), new byte[]{-128, 1});
		Assert.assertEquals(VarIntTool.encode(155), new byte[]{-128 + 27, 1});
		Assert.assertEquals(VarIntTool.encode(Long.MAX_VALUE), LONG_MAX_VALUE_BYTES);
		Assert.assertEquals(VarIntTool.encode(Integer.MAX_VALUE), INT_MAX_VALUE_BYTES);
	}

	@Test
	public void testFromBytes(){
		long maxLong = VarIntTool.decodeLong(LONG_MAX_VALUE_BYTES);
		Assert.assertEquals(maxLong, Long.MAX_VALUE);
		long maxInt = VarIntTool.decodeLong(INT_MAX_VALUE_BYTES);
		Assert.assertEquals(maxInt, Integer.MAX_VALUE);
	}

	@Test
	public void testRoundTrips(){
		var random = new Random();
		for(int i = 0; i < 10000; ++i){
			long value = Math.abs(random.nextLong());
			byte[] bytes = VarIntTool.encode(value);
			long roundTripped = VarIntTool.decodeLong(bytes);
			Assert.assertEquals(roundTripped, value);
		}
	}

	@Test
	public void testInputStreams(){
		var is0 = new ByteArrayInputStream(new byte[]{0});
		long v0 = VarIntTool.fromInputStream(is0).get();
		Assert.assertEquals(v0, 0);

		var is1 = new ByteArrayInputStream(new byte[]{5});
		long v5 = VarIntTool.fromInputStream(is1).get();
		Assert.assertEquals(v5, 5);

		var is2 = new ByteArrayInputStream(new byte[]{-128 + 27, 1});
		long v155 = VarIntTool.fromInputStream(is2).get();
		Assert.assertEquals(v155, 155);

		var is3 = new ByteArrayInputStream(new byte[]{-5, 24});
		long v3195 = VarIntTool.fromInputStream(is3).get();
		Assert.assertEquals(v3195, 3195);
	}

	@Test
	public void testOffset(){
		Assert.assertEquals(VarIntTool.decodeLong(new byte[]{-1, -1, 28}, 2), 28);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidOffsetAndExceptionType(){
		VarIntTool.decodeLong(new byte[]{0, 0, 0}, 4);
	}

	public void testEmptyInputStream(){
		var is = new ByteArrayInputStream(EmptyArray.BYTE);
		Assert.assertEquals(VarIntTool.fromInputStream(is), Optional.empty());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeIntegerAndExceptionType(){
		VarIntTool.encode(-1);
	}

}
