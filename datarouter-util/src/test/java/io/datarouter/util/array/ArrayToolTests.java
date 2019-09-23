/**
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
package io.datarouter.util.array;

import java.util.Arrays;
import java.util.HashSet;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ArrayToolTests{

	@Test
	public void simpleCompare(){
		Double one = 1.0;
		Double two = 2.0;
		Assert.assertEquals(one.compareTo(one), 0);
		Assert.assertEquals(one.compareTo(two), -1);
		Assert.assertEquals(two.compareTo(one), 1);
	}

	@Test
	public void testConcatenateVarargBytes(){
		byte[] concat = ArrayTool.concatenate(new byte[]{0, 1}, new byte[]{2}, new byte[]{3, 4});
		Assert.assertEquals(concat, new byte[]{0, 1, 2, 3, 4});
	}

	@Test
	public void testMapToSet(){
		Assert.assertEquals(ArrayTool.mapToSet(null, (Object)null), new HashSet<>());
		Object nothing = null;
		Assert.assertEquals(ArrayTool.mapToSet(null, nothing), new HashSet<>());
		String[] empty = new String[0];
		Assert.assertEquals(ArrayTool.mapToSet(null, empty), new HashSet<>());
		String[] arr = {"hi", "hello", "hi"};
		Assert.assertEquals(ArrayTool.mapToSet(str -> str + "s", arr), new HashSet<>(Arrays.asList("hellos", "his")));
	}

	@Test
	public void testTrimToSize(){
		byte[] array = new byte[]{0, 1, 2, 3, 4};
		Assert.assertEquals(ArrayTool.trimToSize(new byte[]{}, 2), new byte[2]);
		Assert.assertEquals(ArrayTool.trimToSize(array, 2), new byte[]{0, 1});
		Assert.assertEquals(ArrayTool.trimToSize(array, 5), new byte[]{0, 1, 2, 3, 4});
		Assert.assertEquals(ArrayTool.trimToSize(array, 6), new byte[]{0, 1, 2, 3, 4});
	}

}
