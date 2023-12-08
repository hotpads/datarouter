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
package io.datarouter.bytes.codec.array.bytearray;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TerminatedByteArrayCodecTests{

	public static final TerminatedByteArrayCodec CODEC = TerminatedByteArrayCodec.INSTANCE;

	@Test
	public void testEmpty(){
		var input = new byte[]{};
		var encoded = CODEC.encode(input);
		var expected = new byte[]{0};
		Assert.assertEquals(encoded, expected);
		var decoded = CODEC.decode(encoded, 0).value();
		Assert.assertEquals(decoded, input);
	}

	@Test
	public void testWithoutEscape(){
		var input = new byte[]{5, 6};
		var encoded = CODEC.encode(input);
		var expected = new byte[]{5, 6, 0};
		Assert.assertEquals(encoded, expected);
		var decoded = CODEC.decode(encoded, 0).value();
		Assert.assertEquals(decoded, input);
	}

	@Test
	public void testWithEscape(){
		var input = new byte[]{0, 1};
		var encoded = CODEC.encode(input);
		var expected = new byte[]{1, 2, 1, 3, 0};
		Assert.assertEquals(encoded, expected);
		var decoded = CODEC.decode(encoded, 0).value();
		Assert.assertEquals(decoded, input);
	}

	@Test
	public void testLength(){
		var input1 = new byte[]{0};
		Assert.assertEquals(CODEC.lengthWithTerminalIndex(input1, 0), 1);
		var input2 = new byte[]{5, 0, 5, 5, 0, 5, 0};
		Assert.assertEquals(CODEC.lengthWithTerminalIndex(input2, 2), 3);
		Assert.assertEquals(CODEC.lengthWithTerminalIndex(input2, 5), 2);
	}
}