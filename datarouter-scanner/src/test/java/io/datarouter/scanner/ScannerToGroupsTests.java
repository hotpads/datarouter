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
package io.datarouter.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ScannerToGroupsTests{

	private static final Function<String,String> FIRST = str -> str.substring(0, 1);
	private static final Function<String,String> AFTER_FIRST = str -> str.length() == 1 ? null : str.substring(1);

	@Test
	public void testKeyFunction(){
		Map<String,List<String>> map = Scanner.of("b2", "a1", "b1")
				.groupBy(FIRST);
		Assert.assertSame(map.getClass(), HashMap.class);
		Assert.assertEquals(map.size(), 2);
		Assert.assertSame(map.get("a").getClass(), ArrayList.class);
		Assert.assertEquals(map.get("a"), List.of("a1"));
		Assert.assertEquals(map.get("b"), List.of("b2", "b1"));
	}

	@Test
	public void testValueFunction(){
		Map<String,List<String>> map = Scanner.of("b2", "a1", "b1")
				.groupBy(FIRST, AFTER_FIRST);
		Assert.assertSame(map.getClass(), HashMap.class);
		Assert.assertEquals(map.size(), 2);
		Assert.assertSame(map.get("a").getClass(), ArrayList.class);
		Assert.assertEquals(map.get("a"), List.of("1"));
		Assert.assertEquals(map.get("b"), List.of("2", "1"));
	}

	@Test
	public void testMapSupplier(){
		Map<String,List<String>> map = Scanner.of("b2", "a1", "b1")
				.groupBy(FIRST, AFTER_FIRST, TreeMap::new);
		Assert.assertSame(map.getClass(), TreeMap.class);
		Assert.assertEquals(map.size(), 2);
		Assert.assertEquals(Scanner.of(map.keySet()).list(), List.of("a", "b"));
		Assert.assertSame(map.get("a").getClass(), ArrayList.class);
		Assert.assertEquals(map.get("a"), List.of("1"));
		Assert.assertEquals(map.get("b"), List.of("2", "1"));
	}

	@Test
	public void testCollectionSupplier(){
		Map<String,SortedSet<String>> map = Scanner.of("b2", "a1", "b1")
				.groupBy(FIRST, AFTER_FIRST, TreeMap::new, TreeSet::new);
		Assert.assertSame(map.getClass(), TreeMap.class);
		Assert.assertEquals(map.size(), 2);
		Assert.assertEquals(Scanner.of(map.keySet()).list(), List.of("a", "b"));
		Assert.assertSame(map.get("a").getClass(), TreeSet.class);
		Assert.assertEquals(Scanner.of(map.get("a")).list(), List.of("1"));
		Assert.assertEquals(Scanner.of(map.get("b")).list(), List.of("1", "2"));
	}

}
