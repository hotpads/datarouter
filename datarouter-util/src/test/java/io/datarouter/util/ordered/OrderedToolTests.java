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
package io.datarouter.util.ordered;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.Java9;

public class OrderedToolTests{

	@Test
	public void testSorted1(){
		List<Ordered<String>> list = Java9.listOf(
				new Ordered<>("O", "D"),
				new Ordered<>("D", null),
				new Ordered<>("G", "O"));
		List<String> actual = OrderedTool.sortOrdered(list);
		List<String> expected = Java9.listOf("D", "O", "G");
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testSorted2(){
		List<Ordered<String>> list = Java9.listOf(
				new Ordered<>("A", null),
				new Ordered<>("B", "A"),
				new Ordered<>("C", "B"));
		List<String> actual = OrderedTool.sortOrdered(list);
		List<String> expected = Java9.listOf("A", "B", "C");
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testSorted3(){
		List<Ordered<String>> list = Java9.listOf(
				new Ordered<>("C", "B"),
				new Ordered<>("B", "A"),
				new Ordered<>("A", null));
		List<String> actual = OrderedTool.sortOrdered(list);
		List<String> expected = Java9.listOf("A", "B", "C");
		Assert.assertEquals(actual, expected);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testMultipleNulls(){
		List<Ordered<String>> list = Java9.listOf(
				new Ordered<>("O", null),
				new Ordered<>("D", null),
				new Ordered<>("G", "O"));
		OrderedTool.sortOrdered(list);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testImpossibleOrder(){
		List<Ordered<String>> list = Java9.listOf(
				new Ordered<>("O", "D"),
				new Ordered<>("D", null),
				new Ordered<>("G", "D"));
		OrderedTool.sortOrdered(list);
	}

}
