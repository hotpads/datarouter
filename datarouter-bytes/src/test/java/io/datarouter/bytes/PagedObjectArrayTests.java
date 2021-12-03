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

import java.util.Iterator;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;

public class PagedObjectArrayTests{

	@Test
	public void test(){
		int first = 10;
		int value = first;

		PagedObjectArray<Integer> array = new PagedObjectArray<>(2);
		array.add(value++);
		array.add(value++);
		Assert.assertEquals(array.toArray(), new Integer[]{10, 11});
		array.add(value++);
		array.add(value++);
		array.add(value++);
		Assert.assertEquals(array.toArray(), new Integer[]{10, 11, 12, 13, 14});
		array.add(value++);
		array.add(value++);
		array.add(value++);
		Assert.assertEquals(array.toArray(), new Integer[]{10, 11, 12, 13, 14, 15, 16, 17});

		int length = value - first;

		for(int i = 0; i < length; ++i){
			int actual = array.get(i);
			int expected = first + i;
			Assert.assertEquals(actual, expected);
		}

		Iterator<Integer> iterator = array.iterator();
		for(int i = 0; i < length; ++i){
			Assert.assertTrue(iterator.hasNext());
			int actual = iterator.next();
			int expected = first + i;
			Assert.assertEquals(actual, expected);
		}
		Assert.assertFalse(iterator.hasNext());
	}

	@Test
	public void testToArray(){
		PagedObjectArray<String> pagedArray = new PagedObjectArray<>(2);
		pagedArray.add("hello");
		pagedArray.add("world");

		//convert to Object[]
		Object[] objects = pagedArray.toArray();
		Assert.assertEquals(objects.getClass().getComponentType(), Object.class);
		boolean errored = false;
		try{
			String[] castedBad = (String[])pagedArray.toArray();
			Assert.assertEquals(castedBad.length, 2);//Shouldn't get here
		}catch(Exception e){
			errored = true;
		}
		Assert.assertTrue(errored);

		//convert to String[]
		String[] strings = pagedArray.toArray(new String[0]);
		Assert.assertEquals(strings.getClass().getComponentType(), String.class);
		String[] castedGood = pagedArray.toArray(new String[0]);
		Assert.assertEquals(castedGood.length, 2);
	}

	@Test
	public void testWithScanner(){
		List<Integer> expected = Scanner.iterate(0, i -> i + 1)
				.limit(100)
				.list();
		PagedObjectArray<Integer> actual = Scanner.of(expected)
				.collect(PagedObjectArray::new);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testContains(){
		List<Integer> array = Scanner.iterate(0, i -> i + 1)
				.limit(10)
				.list();
		Assert.assertTrue(array.contains(3));
		Assert.assertFalse(array.contains(300));
		Assert.assertTrue(array.containsAll(Java9.listOf(3, 5)));
		Assert.assertFalse(array.containsAll(Java9.listOf(3, 5, 300)));
	}

}
