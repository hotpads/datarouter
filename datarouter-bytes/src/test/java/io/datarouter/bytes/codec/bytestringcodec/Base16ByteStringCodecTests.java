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
package io.datarouter.bytes.codec.bytestringcodec;

import java.nio.charset.StandardCharsets;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.ByteTool;

public class Base16ByteStringCodecTests{

	private static final Base16ByteStringCodec CODEC = Base16ByteStringCodec.INSTANCE;

	@Test
	public void testEncode(){
		byte[] textBytes = "hello world!".getBytes(StandardCharsets.UTF_8);
		byte[] allBytes = ByteTool.concatenate(textBytes, new byte[]{0, 127, -128});
		String hexString = CODEC.encode(allBytes);
		Assert.assertEquals(hexString, "68656c6c6f20776f726c6421007f80");
	}

}
