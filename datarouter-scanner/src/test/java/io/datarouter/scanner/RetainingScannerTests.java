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
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RetainingScannerTests{

	@Test
	public void testEmptyInputScanner(){
		Scanner<RetainingGroup<Integer>> retainingScanner = Scanner.of(new ArrayList<Integer>()).retain(3);
		Assert.assertFalse(retainingScanner.advance());
	}

	@Test
	public void testRetainingZero(){
		Scanner<RetainingGroup<Integer>> retainingScanner = Scanner.of(1, 2).retain(0);
		List<RetainingGroup<Integer>> groups = retainingScanner.list();
		Assert.assertEquals(groups.size(), 2);
		Assert.assertEquals(groups.get(0).current().intValue(), 1);
		Assert.assertEquals(groups.get(1).current().intValue(), 2);
	}

	@Test
	public void testRetainingOne(){
		Scanner<RetainingGroup<Integer>> retainingScanner = Scanner.of(1, 2).retain(1);
		List<RetainingGroup<Integer>> groups = retainingScanner.list();
		Assert.assertEquals(groups.size(), 2);
		Assert.assertEquals(groups.get(0).current().intValue(), 1);
		Assert.assertEquals(groups.get(0).peekBack(1), null);
		Assert.assertEquals(groups.get(1).current().intValue(), 2);
		Assert.assertEquals(groups.get(1).peekBack(1).intValue(), 1);
	}


	@Test
	public void testRetainingTwo(){
		Scanner<RetainingGroup<Integer>> retainingScanner = Scanner.of(1, 2).retain(2);
		List<RetainingGroup<Integer>> groups = retainingScanner.list();
		Assert.assertEquals(groups.size(), 2);
		Assert.assertEquals(groups.get(0).current().intValue(), 1);
		Assert.assertEquals(groups.get(0).peekBack(1), null);
		Assert.assertEquals(groups.get(0).peekBack(2), null);
		Assert.assertEquals(groups.get(1).current().intValue(), 2);
		Assert.assertEquals(groups.get(1).peekBack(1).intValue(), 1);
		Assert.assertEquals(groups.get(1).peekBack(2), null);
	}

	@Test
	public void indexOutOfBoundErrorWhenPeekingTooFarBack(){
		RetainingGroup<Integer> retainingGroup = Scanner.of(1).retain(1).findFirst().get();
		Assert.assertEquals(retainingGroup.current().intValue(), 1);
		Assert.assertEquals(retainingGroup.peekBack(1), null);
		Assert.assertThrows(IndexOutOfBoundsException.class, () -> retainingGroup.peekBack(2));
	}

}
