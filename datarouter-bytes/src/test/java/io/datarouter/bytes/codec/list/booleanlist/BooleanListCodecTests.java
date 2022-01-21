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
package io.datarouter.bytes.codec.list.booleanlist;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class BooleanListCodecTests{

	private static final BooleanListCodec BOOLEAN_LIST_CODEC = BooleanListCodec.INSTANCE;

	@Test
	public void testToFromByteArray(){
		boolean one = true;
		boolean two = false;
		boolean three = false;
		List<Boolean> booleans = Arrays.asList(one, null, null, two, three);

		byte[] booleanBytes = BOOLEAN_LIST_CODEC.encode(booleans);
		List<Boolean> result = BOOLEAN_LIST_CODEC.decode(booleanBytes, 0);
		Assert.assertEquals(result, booleans);
	}

}
