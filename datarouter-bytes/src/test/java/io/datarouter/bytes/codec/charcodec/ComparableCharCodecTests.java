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
package io.datarouter.bytes.codec.charcodec;

import java.util.Arrays;
import java.util.TreeSet;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ComparableCharCodecTests{

	private static final ComparableCharCodec CODEC = ComparableCharCodec.INSTANCE;

	//illustrating chars cast to positive ints: 0 to 65_535
	@Test
	public void testCasting(){
		Assert.assertEquals(0, Character.MIN_VALUE);
		Assert.assertEquals(65535, Character.MAX_VALUE);
		int intValue = 0;
		for(char c = Character.MIN_VALUE; c <= Character.MAX_VALUE; ++c){
			int charAsInt = c;
			Assert.assertEquals(intValue, charAsInt);
			++intValue;
			if(c == Character.MAX_VALUE){
				break;
			}
		}
	}

	@Test
	public void testCodec(){
		var seen = new TreeSet<byte[]>(Arrays::compareUnsigned);
		Character previousChar = null;
		byte[] previousBytes = null;
		for(char c = Character.MIN_VALUE; c <= Character.MAX_VALUE; ++c){
			byte[] bytes = CODEC.encode(c);
			Assert.assertFalse(seen.contains(bytes));
			seen.add(bytes);
			if(previousChar != null){
				Assert.assertTrue(Character.compare(previousChar, c) < 0);
			}
			if(previousBytes != null){
				Assert.assertTrue(Arrays.compareUnsigned(previousBytes, bytes) < 0);
			}
			char c2 = CODEC.decode(bytes);
			Assert.assertEquals(c2, c);
			if(c == Character.MAX_VALUE){
				break;
			}
			previousChar = c;
			previousBytes = bytes;
		}
	}

}
