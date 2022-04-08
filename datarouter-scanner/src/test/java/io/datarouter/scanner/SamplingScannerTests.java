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

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SamplingScannerTests{

	private static final List<Integer> EVEN_INPUTS = List.of(0, 1, 2, 3);
	private static final List<Integer> ODD_INPUTS = List.of(0, 1, 2, 3, 4);

	@Test
	public void testEvenExcludeLast(){
		List<Integer> actual = Scanner.of(EVEN_INPUTS)
				.sample(2, false)
				.list();
		List<Integer> expected = List.of(1, 3);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testEvenIncludeLast(){
		List<Integer> actual = Scanner.of(EVEN_INPUTS)
				.sample(2, true)
				.list();
		List<Integer> expected = List.of(1, 3);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testOddExcludeLast(){
		List<Integer> actual = Scanner.of(ODD_INPUTS)
				.sample(2, false)
				.list();
		List<Integer> expected = List.of(1, 3);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testOddIncludeLast(){
		List<Integer> actual = Scanner.of(ODD_INPUTS)
				.sample(2, true)
				.list();
		List<Integer> expected = List.of(1, 3, 4);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testEmptyIncludeLast(){
		List<Integer> actual = Scanner.<Integer>empty()
				.sample(2, true)
				.list();
		List<Integer> expected = List.of();
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testEmptyExcludeLast(){
		List<Integer> actual = Scanner.<Integer>empty()
				.sample(2, false)
				.list();
		List<Integer> expected = List.of();
		Assert.assertEquals(actual, expected);
	}

}
