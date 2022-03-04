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
package io.datarouter.util.array;

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
	public void testTrimToSize(){
		byte[] array = {0, 1, 2, 3, 4};
		Assert.assertEquals(ArrayTool.trimToSize(new byte[]{}, 2), new byte[2]);
		Assert.assertEquals(ArrayTool.trimToSize(array, 2), new byte[]{0, 1});
		Assert.assertEquals(ArrayTool.trimToSize(array, 5), new byte[]{0, 1, 2, 3, 4});
		Assert.assertEquals(ArrayTool.trimToSize(array, 6), new byte[]{0, 1, 2, 3, 4});
	}

}
