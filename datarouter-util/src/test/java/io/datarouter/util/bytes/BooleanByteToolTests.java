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
package io.datarouter.util.bytes;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class BooleanByteToolTests{

	@Test
	public void testGetBytes(){
		Assert.assertEquals(BooleanByteTool.getBytes(false)[0], 0);
		Assert.assertEquals(BooleanByteTool.getBytes(true)[0], -1);
	}

	@Test
	public void testFromBytes(){
		Assert.assertEquals(BooleanByteTool.fromBytes(new byte[]{0}, 0), false);
		Assert.assertEquals(BooleanByteTool.fromBytes(new byte[]{1, 0}, 1), false);
		Assert.assertEquals(BooleanByteTool.fromBytes(new byte[]{-37}, 0), true);
	}

	@Test
	public void testToFromByteArray(){
		boolean one = true;
		boolean two = false;
		boolean three = false;

		List<Boolean> booleans = new ArrayList<>();
		booleans.add(one);
		booleans.add(null);
		booleans.add(null);
		booleans.add(two);
		booleans.add(three);

		byte[] booleanBytes = BooleanByteTool.getBooleanByteArray(booleans);
		List<Boolean> result = BooleanByteTool.fromBooleanByteArray(booleanBytes, 0);
		Assert.assertEquals(result, booleans);
	}

}
