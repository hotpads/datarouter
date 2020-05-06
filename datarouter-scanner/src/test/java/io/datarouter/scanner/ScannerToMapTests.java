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

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.ScannerToMap.Replace;

public class ScannerToMapTests{

	private static final Function<String,String> FIRST = str -> str.substring(0, 1);
	private static final Function<String,String> AFTER_FIRST = str -> str.length() == 1 ? null : str.substring(1);

	@Test
	public void test(){
			Map<String,String> map = Scanner.of("cc", "aa", "bb")
					.toMap(FIRST, AFTER_FIRST);
			Assert.assertSame(map.getClass(), HashMap.class);
			Assert.assertEquals(map.size(), 3);
			Assert.assertEquals(map.get("a"), "a");
			Assert.assertEquals(map.get("b"), "b");
			Assert.assertEquals(map.get("c"), "c");
	}

	@Test
	public void testSorted(){
			SortedMap<String,String> map = Scanner.of("cc", "aa", "bb")
					.toMap(FIRST, AFTER_FIRST, TreeMap::new);
			Assert.assertSame(map.getClass(), TreeMap.class);
			Assert.assertEquals(map.size(), 3);
			Assert.assertEquals(map.get("a"), "a");
			Assert.assertEquals(map.get("b"), "b");
			Assert.assertEquals(map.get("c"), "c");
	}

	@Test
	public void testReplaceAlways(){
		Map<String,String> map = Scanner.of("a1", "b1", "b2")
				.to(ScannerToMap.of(FIRST, AFTER_FIRST));
		Assert.assertEquals(map.get("a"), "1");
		Assert.assertEquals(map.get("b"), "2");
	}

	@Test
	public void testReplaceNullValues(){
		Map<String,String> map = Scanner.of("a1", "b", "b2")
				.to(ScannerToMap.of(FIRST, AFTER_FIRST, Replace.NULL_VALUES, HashMap::new));
		Assert.assertEquals(map.get("a"), "1");
		Assert.assertEquals(map.get("b"), "2");
	}

	@Test
	public void testReplaceNullKeys(){
		Map<String,String> map = Scanner.of("a1", "b", "b2")
				.to(ScannerToMap.of(FIRST, AFTER_FIRST, Replace.NULL_KEYS, HashMap::new));
		Assert.assertEquals(map.get("a"), "1");
		Assert.assertEquals(map.get("b"), null);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testReplaceNever(){
		Scanner.of("a1", "b1", "b2")
				.to(ScannerToMap.of(FIRST, AFTER_FIRST, Replace.NEVER, HashMap::new));
	}

	@Test
	public void testMerge(){
		Map<String,String> map = Scanner.of("a1", "b1", "b2")
				.to(ScannerToMap.of(FIRST, AFTER_FIRST, (a, b) -> a + b, HashMap::new));
		Assert.assertEquals(map.get("a"), "1");
		Assert.assertEquals(map.get("b"), "12");
	}

}
