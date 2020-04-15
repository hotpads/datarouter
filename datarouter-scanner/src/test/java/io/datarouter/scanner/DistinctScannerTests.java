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

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DistinctScannerTests{

	@Test
	public void testDistinct(){
		Scanner<Integer> input = Scanner.of(3, 1, 4, 2, 1, 2, 2, 1, 4);
		List<Integer> expected = Arrays.asList(1, 2, 3, 4);
		List<Integer> actual = input.distinct().list();
		Assert.assertEquals(actual.size(), 4);
		Assert.assertTrue(actual.containsAll(expected));
	}

	@Test
	public void testDistinctBy(){
		Scanner<String> input = Scanner.of("aa", "ab", "bb", "ba");
		List<String> expected = Arrays.asList("aa", "bb");
		List<String> actual = input.distinctBy(string -> string.substring(0, 1)).list();
		Assert.assertEquals(actual.size(), 2);
		Assert.assertTrue(actual.containsAll(expected));
	}

}
