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
package io.datarouter.scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PagedListTests{

	@Test
	public void testCreate(){
		List<Integer> arrayList = Scanner.iterate(0, i -> i + 1)
				.limit(100)
				.collect(ArrayList::new);
		List<Integer> pagedList = Scanner.iterate(0, i -> i + 1)
				.limit(100)
				.collect(PagedList::new);
		Assert.assertEquals(pagedList.size(), 100);
		Assert.assertEquals(pagedList, arrayList);
	}

	@Test
	public void testAddAtIndex(){
		var list = new PagedList<>(2);
		list.addAll(List.of(0, 1, 2));
		list.add(1, 99);
		Assert.assertEquals(list, List.of(0, 99, 1, 2));
	}

	@Test
	public void testSet(){
		var list = new PagedList<Integer>(2);
		list.addAll(List.of(0, 1, 2));
		int previous = list.set(1, 99);
		Assert.assertEquals(previous, 1);
		Assert.assertEquals(list, List.of(0, 99, 2));
	}

	@Test
	public void testRemove(){
		var list = new PagedList<Integer>(2);
		list.addAll(List.of(0, 1, 2));
		int previous = list.remove(1);
		Assert.assertEquals(previous, 1);
		Assert.assertEquals(list, List.of(0, 2));
	}

	@Test
	public void testSort(){
		var list = new PagedList<Integer>(2);
		list.addAll(List.of(3, 5, 1, 2, 0, 4));
		Collections.sort(list);
		Assert.assertEquals(list, List.of(0, 1, 2, 3, 4, 5));
	}

}
