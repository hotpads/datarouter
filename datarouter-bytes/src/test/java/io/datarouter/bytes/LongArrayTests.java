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

import java.util.LinkedList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LongArrayTests{

	@Test
	public void testBasics(){
		List<Long> list = new LongArray();
		final int max = 150;

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
		Assert.assertEquals(valueRemoved, 31L);
		Assert.assertEquals(list.size(), max - 1);
		Assert.assertEquals(list.get(31).longValue(), 32L);

		// removing objects
		boolean modified = list.remove(5L);
		Assert.assertTrue(modified);
		Assert.assertEquals(list.size(), max - 2);
		Assert.assertEquals(list.get(5).longValue(), 6L);
		modified = list.remove(5L);
		Assert.assertFalse(modified);
		Assert.assertEquals(list.size(), max - 2);
		Assert.assertEquals(list.get(5).longValue(), 6L);

		// retain objects
		List<Long> toRetain = Java9.listOf(55L, 57L, 7L);
		modified = list.retainAll(toRetain);
		Assert.assertTrue(modified);
		Assert.assertEquals(list.size(), 3);
		Assert.assertEquals(list.get(0).longValue(), 7L);
		Assert.assertEquals(list.get(1).longValue(), 55L);
		Assert.assertEquals(list.get(2).longValue(), 57L);
		modified = list.retainAll(Java9.listOf(55L, 57L, 7L));
		Assert.assertFalse(modified);
		Assert.assertEquals(list.size(), 3);
		Assert.assertEquals(list.get(0).longValue(), 7L);
		Assert.assertEquals(list.get(1).longValue(), 55L);
		Assert.assertEquals(list.get(2).longValue(), 57L);

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
		Assert.assertEquals(nullableList.size(), 5);
		Assert.assertEquals(primitiveList.size(), 5);
		Assert.assertNull(primitiveList.get(1));
	}

	@Test
	public void testSort(){
		LongArray list = new LongArray(new long[]{3, 1, 2, 4});
		list.sortInPlace();
		Assert.assertEquals(list, new LongArray(new long[]{1, 2, 3, 4}));
	}

	@Test
	public void testDeduplicate(){
		LongArray list = new LongArray(new long[]{3, 3, 1, 2, 1, 1});
		LongArray deduped = list.copyDedupeConsecutive();
		Assert.assertEquals(deduped, new LongArray(new long[]{3, 1, 2, 1}));
		LongArray sortedDeduped = list.sortInPlace().copyDedupeConsecutive();
		Assert.assertEquals(sortedDeduped, new LongArray(new long[]{1, 2, 3}));
	}

	@Test
	public void testAddByIndex(){
		LongArray list = new LongArray(new long[]{1, 2, 3, 4, 5, 6, 7});
		list.add(7, 700);
		Assert.assertEquals(list.size(), 8);
		list.add(2, 200);
		list.add(0, -100);
		Assert.assertEquals(list.size(), 10);
		Assert.assertEquals(list, new LongArray(new long[]{-100, 1, 2, 200, 3, 4, 5, 6, 7, 700}));
		Assert.assertEquals(list.get(0).longValue(), -100L);
		Assert.assertEquals(list.get(1).longValue(), 1L);
		Assert.assertEquals(list.get(3).longValue(), 200L);
		Assert.assertEquals(list.get(6).longValue(), 5L);
	}

	@Test
	public void testAddAllByIndex(){
		LongArray list = new LongArray(new long[]{1, 2, 3, 4, 5, 6, 7});
		list.addAll(7, new LongArray(new long[]{700}));
		Assert.assertEquals(list.size(), 8);
		list.addAll(2, new LongArray(new long[]{200, 201}));
		list.addAll(0, new LongArray(new long[]{-100, -101, -102}));
		Assert.assertEquals(list.size(), 13);
		Assert.assertEquals(list, new LongArray(new long[]{-100, -101, -102, 1, 2, 200, 201, 3, 4, 5, 6, 7, 700}));
		Assert.assertEquals(list.get(0).longValue(), -100L);
		Assert.assertEquals(list.get(3).longValue(), 1L);
		Assert.assertEquals(list.get(6).longValue(), 201L);
		Assert.assertEquals(list.get(10).longValue(), 6L);
	}

	@Test
	public void testSetByIndex(){
		LongArray list = new LongArray(new long[]{1, 2, 3, 4, 5, 6, 7});
		list.set(6, 700L);
		Assert.assertEquals(list.size(), 7);
		list.set(2, 300L);
		list.set(0, -100L);
		Assert.assertEquals(list.size(), 7);
		Assert.assertEquals(list, new LongArray(new long[]{-100, 2, 300, 4, 5, 6, 700}));
		Assert.assertEquals(list.get(0).longValue(), -100L);
		Assert.assertEquals(list.get(2).longValue(), 300L);
		Assert.assertEquals(list.get(6).longValue(), 700L);
	}
}