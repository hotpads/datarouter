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
package io.datarouter.bytes.primitivelist;

import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PrimitiveIntListTests{

	@Test
	public void testNulls(){
		var list = new PrimitiveIntList(new int[]{2});
		Assert.assertThrows(NullPointerException.class, () -> list.set(0, null));
	}

	@Test
	public void testEmptyEquals(){
		List<Integer> first = List.of();
		var second = new PrimitiveIntList(new int[]{});
		Assert.assertEquals(first, second);
		Assert.assertEquals(second, first);
		Assert.assertTrue(first.equals(second));
		Assert.assertTrue(second.equals(first));
	}

	@Test
	public void testEquals(){
		List<Integer> first = List.of(1, 2);
		var second = new PrimitiveIntList(new int[]{-1, 1, 2, 100}, 1, 3);
		Assert.assertEquals(first, second);
		Assert.assertEquals(second, first);
	}

	@Test
	public void testSetByIndex(){
		var list = new PrimitiveIntList(new int[]{-2, 1, 2, 3, 4, 5, 6, 7, 100}, 1, 8);
		list.set(6, 700);
		Assert.assertEquals(list.size(), 7);
		list.set(2, 300);
		list.set(0, -100);
		Assert.assertEquals(list.size(), 7);
		Assert.assertEquals(list, new PrimitiveIntList(new int[]{-100, 2, 300, 4, 5, 6, 700}));
		Assert.assertEquals(list.get(0).longValue(), -100);
		Assert.assertEquals(list.get(2).longValue(), 300);
		Assert.assertEquals(list.get(6).longValue(), 700);
	}

	@Test
	public void testCollectionsSort(){
		var list = new PrimitiveIntList(new int[]{-2, -1, 5, 3, 4, 0, 2, 1, 100}, 2, 8);
		Collections.sort(list);
		Assert.assertEquals(list, new PrimitiveIntList(new int[]{0, 1, 2, 3, 4, 5}));
	}

	@Test
	public void testSubList(){
		int[] array = {-2, -1, 0, 1, 2, 3, 4};
		var list = new PrimitiveIntList(array, 2, 5);
		Assert.assertEquals(list, List.of(0, 1, 2));
		var subList = list.subList(1, 3);
		Assert.assertEquals(subList, List.of(1, 2));
	}

}
