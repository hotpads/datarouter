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

import org.testng.Assert;
import org.testng.annotations.Test;

public class PagedObjectArrayTests{

	@Test
	public void test(){
		int first = 10;
		int value = first;

		PagedObjectArray<Integer> writer = new PagedObjectArray<>(2);
		writer.add(value++);
		writer.add(value++);
		Assert.assertEquals(writer.concat(), new Integer[]{10, 11});
		writer.add(value++);
		writer.add(value++);
		writer.add(value++);
		Assert.assertEquals(writer.concat(), new Integer[]{10, 11, 12, 13, 14});
		writer.add(value++);
		writer.add(value++);
		writer.add(value++);
		Assert.assertEquals(writer.concat(), new Integer[]{10, 11, 12, 13, 14, 15, 16, 17});

		int length = value - first;

		for(int i = 0; i < length; ++i){
			int actual = writer.get(i);
			int expected = first + i;
			Assert.assertEquals(actual, expected);
		}
	}

}