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

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class IntegerByteToolTests{

	//verify that -128 in bytes gets converted to -128 long.  Bitwise cast would be +128
	@Test
	public void testCasting(){
		byte b0 = 0, b1 = 1, b127 = 127, bn128 = -128, bn1 = -1;
		Assert.assertEquals(b0, 0L);
		Assert.assertEquals(b1, 1L);
		Assert.assertEquals(b127, 127L);
		Assert.assertEquals(bn128, -128L);
		Assert.assertEquals(bn1, -1L);
	}

	@Test
	public void testToFromByteArray(){
		int one = -239483;
		int two = 583;

		List<Integer> integers = new ArrayList<>();
		integers.add(one);
		integers.add(null);
		integers.add(two);

		byte[] integerBytes = IntegerByteTool.getIntegerByteArray(integers);
		List<Integer> result = IntegerByteTool.fromIntegerByteArray(integerBytes, 0);
		Assert.assertEquals(result, integers);
	}

}
