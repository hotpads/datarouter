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
package io.datarouter.bytes.codec.list.intlist;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class IntListCodecTests{

	private static final IntListCodec INT_LIST_CODEC = IntListCodec.INSTANCE;

	@Test
	public void testToFromByteArray(){
		int one = -239483;
		int two = 583;

		List<Integer> integers = new ArrayList<>();
		integers.add(one);
		integers.add(null);
		integers.add(two);

		byte[] integerBytes = INT_LIST_CODEC.encode(integers);
		List<Integer> result = INT_LIST_CODEC.decode(integerBytes, 0);
		Assert.assertEquals(result, integers);
	}

}
