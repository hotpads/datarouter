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
package io.datarouter.bytes.codec.bytestring;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.bytestringcodec.HexByteStringCodec;

/**
 * Test compatibility between:
 * - io.datarouter.bytes.codec.bytestringcodec.encode(byte[])
 * - org.apache.commons.codec.binary.Hex.encodeHexString(byte[])
 */
public class HexByteStringCodecTests{

	private static final HexByteStringCodec CODEC = HexByteStringCodec.INSTANCE;

	private record HexPair(
			String hex,
			byte[] bytes){
	}

	private static final List<HexPair> PAIRS = List.of(
			new HexPair("", EmptyArray.BYTE),
			new HexPair("00", new byte[]{0}),
			new HexPair("01", new byte[]{1}),
			new HexPair("09", new byte[]{9}),
			new HexPair("0f", new byte[]{15}),
			new HexPair("1f", new byte[]{31}),
			new HexPair("fe", new byte[]{-2}),
			new HexPair("ff", new byte[]{-1}),
			new HexPair("0000", new byte[]{0, 0}),
			new HexPair("000f", new byte[]{0, 15}),
			new HexPair("121a", new byte[]{18, 26}));

	@Test
	public void testEncode(){
		PAIRS.forEach(pair -> Assert.assertEquals(CODEC.encode(pair.bytes), pair.hex));
	}

	@Test
	public void testDecode(){
		PAIRS.forEach(pair -> Assert.assertEquals(CODEC.decode(pair.hex), pair.bytes));
	}

	@Test
	public void testInvalid(){
		Assert.assertThrows(IllegalArgumentException.class, () -> CODEC.decode("1"));// must be even length
		Assert.assertThrows(IllegalArgumentException.class, () -> CODEC.decode("0g"));// must be 0-9 or a-f
		Assert.assertThrows(IllegalArgumentException.class, () -> CODEC.decode("0A"));// must be lowercase
	}

}