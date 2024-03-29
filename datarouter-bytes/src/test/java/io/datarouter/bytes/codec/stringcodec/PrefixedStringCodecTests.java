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
package io.datarouter.bytes.codec.stringcodec;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PrefixedStringCodecTests{

	private static final PrefixedStringCodec CODEC = PrefixedStringCodec.UTF_8;

	@Test
	public void test(){
		String string = "abc";
		byte[] expectedBytes = {3, 'a', 'b', 'c'};
		byte[] actualBytes = CODEC.encode(string);
		Assert.assertEquals(actualBytes, expectedBytes);
		String actualString = CODEC.decode(actualBytes);
		Assert.assertEquals(actualString, string);
	}

}
