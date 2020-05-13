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

import java.util.LinkedList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LongArrayTests{

	private List<Long> list = new LongArray();
	private int max = 150;

	@Test
	public void testBasics(){

		// expanding
		for(long l = 0; l < max; ++l){
			list.add(l);
			Assert.assertEquals(list.size() - 1, l);
		}
		Assert.assertEquals(list.size(), max);

		// shrinking
		for(long l = max - 1; l >= 0; --l){
			list.remove(l);
			Assert.assertEquals(list.size(), l);
		}
		Assert.assertEquals(list.size(), 0);

		// re-expanding
		for(long l = 0; l < max; ++l){
			list.add(l);
			Assert.assertEquals(list.size() - 1, l);
		}

		// removing indexes
		long valueRemoved = list.remove(31);
		Assert.assertTrue(31L == valueRemoved);
		Assert.assertEquals(list.size(), max - 1);
		Assert.assertTrue(list.get(31).equals(32L));

		// removing objects
		boolean modified = list.remove(5L);
		Assert.assertTrue(modified);
		Assert.assertEquals(list.size(), max - 2);
		Assert.assertTrue(list.get(5).equals(6L));
		modified = list.remove(5L);
		Assert.assertFalse(modified);
		Assert.assertEquals(list.size(), max - 2);
		Assert.assertTrue(list.get(5).equals(6L));

		// retain objects
		List<Long> toRetain = List.of(55L, 57L, 7L);
		modified = list.retainAll(toRetain);
		Assert.assertTrue(modified);
		Assert.assertEquals(list.size(), 3);
		Assert.assertTrue(list.get(0).equals(7L));
		Assert.assertTrue(list.get(1).equals(55L));
		Assert.assertTrue(list.get(2).equals(57L));
		modified = list.retainAll(List.of(55L, 57L, 7L));
		Assert.assertFalse(modified);
		Assert.assertEquals(list.size(), 3);
		Assert.assertTrue(list.get(0).equals(7L));
		Assert.assertTrue(list.get(1).equals(55L));
		Assert.assertTrue(list.get(2).equals(57L));

		// nulls
		List<Long> nullableList = new LinkedList<>();
		LongArray primitiveList = new LongArray();
		nullableList.add(-7L);
		primitiveList.add(-7L);
		nullableList.add(null);
		primitiveList.add(null);
		nullableList.add(-8L);
		primitiveList.add(-8L);
		nullableList.add(null);
		primitiveList.add(null);
		nullableList.add(Long.MAX_VALUE);
		primitiveList.add(Long.MAX_VALUE);
		Assert.assertTrue(5 == nullableList.size());
		Assert.assertTrue(5 == primitiveList.size());
		Assert.assertTrue(0 == primitiveList.compareTo(nullableList));
		Assert.assertNull(primitiveList.get(1));
	}

}