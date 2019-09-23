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

import io.datarouter.util.collection.ListTool;

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
	public void testBetween(){
		Assert.assertTrue(ComparableTool.between(-3f, false, -1f, 7f, false));
		Assert.assertFalse(ComparableTool.between(-3f, false, -17.5f, 7f, false));
		Assert.assertTrue(ComparableTool.between(-3f, true, -3f, 7f, false));
		Assert.assertTrue(ComparableTool.between(0, true, 0, 0, false));
		// treat start=null as -Infinity, end=null as Infinity
		Assert.assertTrue(ComparableTool.between(null, true, 12345, null, true));
	}

	@Test
	public void testIsSorted(){
		Assert.assertTrue(ComparableTool.isSorted(null));
		Assert.assertTrue(ComparableTool.isSorted(new ArrayList<Integer>()));
		List<Integer> listA = ListTool.create(1,2,3,4);
		Assert.assertTrue(ComparableTool.isSorted(listA));
		List<Integer> listB = ListTool.create(1,2,55,4);
		Assert.assertFalse(ComparableTool.isSorted(listB));
	}

	@Test
	public void testMin(){
		Assert.assertNull(ComparableTool.min((Integer)null));
		Assert.assertNull(ComparableTool.min((Integer)null, null));
		Assert.assertNull(ComparableTool.min((Integer)null, null, null));
		Assert.assertNull(ComparableTool.min((Double)null));
		Assert.assertNull(ComparableTool.min((Double)null, null));
		Assert.assertNull(ComparableTool.min((Double)null, null, null));
		Assert.assertEquals(ComparableTool.min(null, null, 3), Integer.valueOf(3));
		Assert.assertEquals(ComparableTool.min(3, null), Integer.valueOf(3));
		Assert.assertEquals(ComparableTool.min(3, 1), Integer.valueOf(1));
		Assert.assertEquals(ComparableTool.min(1, 3), Integer.valueOf(1));
		Assert.assertEquals(ComparableTool.min(1, 2, 3), Integer.valueOf(1));
		Assert.assertEquals(ComparableTool.min(1, 3, 2), Integer.valueOf(1));
		Assert.assertEquals(ComparableTool.min(1, 3, null, 2), Integer.valueOf(1));
		Assert.assertEquals(ComparableTool.min(3, null, 2), Integer.valueOf(2));
		Assert.assertEquals(ComparableTool.min(null, null, 3d), Double.valueOf(3));
		Assert.assertEquals(ComparableTool.min(3d, null), Double.valueOf(3));
		Assert.assertEquals(ComparableTool.min(3d, 1d), Double.valueOf(1));
		Assert.assertEquals(ComparableTool.min(1d, 3d), Double.valueOf(1));
		Assert.assertEquals(ComparableTool.min(1d, 2d, 3d), Double.valueOf(1));
		Assert.assertEquals(ComparableTool.min(1d, 3d, 2d), Double.valueOf(1));
		Assert.assertEquals(ComparableTool.min(1d, 3d, null, 2d), Double.valueOf(1));
		Assert.assertEquals(ComparableTool.min(3d, null, 2d), Double.valueOf(2));
	}

	@Test
	public void testMax(){
		Assert.assertNull(ComparableTool.max((Integer)null, null));
		Assert.assertEquals(ComparableTool.max(null, 0), Integer.valueOf(0));
		Assert.assertEquals(ComparableTool.max(0, null), Integer.valueOf(0));
		Assert.assertEquals(ComparableTool.max(0, 0), Integer.valueOf(0));
		Assert.assertEquals(ComparableTool.max(0, 3), Integer.valueOf(3));
		Assert.assertEquals(ComparableTool.max(3, 0), Integer.valueOf(3));
		Assert.assertNull(ComparableTool.max((Double)null));
		Assert.assertNull(ComparableTool.max((Double)null, null));
		Assert.assertNull(ComparableTool.max((Double)null, null, null));
		Assert.assertEquals(ComparableTool.max(null, null, 3d), Double.valueOf(3));
		Assert.assertEquals(ComparableTool.max(3d, null), Double.valueOf(3));
		Assert.assertEquals(ComparableTool.max(3d, 1d), Double.valueOf(3));
		Assert.assertEquals(ComparableTool.max(1d, 3d), Double.valueOf(3));
		Assert.assertEquals(ComparableTool.max(1d, 2d, 3d), Double.valueOf(3));
		Assert.assertEquals(ComparableTool.max(1d, 3d, 2d), Double.valueOf(3));
		Assert.assertEquals(ComparableTool.max(1d, 3d, null, 2d), Double.valueOf(3));
		Assert.assertEquals(ComparableTool.max(1d, null, 2d), Double.valueOf(2));
	}

}
