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
package io.datarouter.util;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ComparableToolTests{

	@Test
	public void testLessThan(){
		Assert.assertTrue(ComparableTool.lt("a", "b"));
		Assert.assertTrue(ComparableTool.lt(null, "b"));
		Assert.assertFalse(ComparableTool.lt("a", null));
		Assert.assertFalse(ComparableTool.lt(null, null));
		Assert.assertFalse(ComparableTool.lt("eq", "eq"));
	}

	@Test
	public void testGreaterThan(){
		Assert.assertFalse(ComparableTool.gt("a", "b"));
		Assert.assertFalse(ComparableTool.gt(null, "b"));
		Assert.assertTrue(ComparableTool.gt("a", null));
		Assert.assertFalse(ComparableTool.gt(null, null));
		Assert.assertFalse(ComparableTool.gt("eq", "eq"));
	}

	@Test
	public void testIsSorted(){
		Assert.assertTrue(ComparableTool.isSorted(null));
		Assert.assertTrue(ComparableTool.isSorted(new ArrayList<Integer>()));
		List<Integer> listA = Java9.listOf(1,2,3,4);
		Assert.assertTrue(ComparableTool.isSorted(listA));
		List<Integer> listB = Java9.listOf(1,2,55,4);
		Assert.assertFalse(ComparableTool.isSorted(listB));
	}

}
