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

import java.util.List;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ScannerTests{

	@Test
	public void testScannerOf(){
		Assert.assertSame(Scanner.of().getClass(), EmptyScanner.class);
		Assert.assertSame(Scanner.of(1).getClass(), ObjectScanner.class);
		Assert.assertSame(Scanner.of(1, 2).getClass(), ArrayScanner.class);
		Assert.assertSame(Scanner.of(List.of(1)).getClass(), IteratorScanner.class);
		Assert.assertSame(Scanner.of(List.of(1).iterator()).getClass(), IteratorScanner.class);
		Assert.assertSame(Scanner.of(Stream.of(1)).getClass(), StreamScanner.class);
	}

	@Test
	public void testConcatIterables(){
		List<Integer> iter1 = List.of(1);
		List<Integer> iter2 = List.of();
		List<Integer> iter3 = List.of(1, 2);
		List<Integer> actual = Scanner.concat(iter1, iter2, iter3).list();
		Assert.assertEquals(actual, List.of(1, 1, 2));
	}

	@Test
	public void testAppendScanner(){
		List<Integer> actual = Scanner.of(1).append(Scanner.of(List.of(1, 2))).list();
		Assert.assertEquals(actual, List.of(1, 1, 2));
	}

	@Test
	public void testAppendVarargs(){
		List<Integer> actual = Scanner.of(1).append(1, 2).list();
		Assert.assertEquals(actual, List.of(1, 1, 2));
	}

	@Test
	public void testAppendIterable(){
		List<Integer> actual = Scanner.of(1).append(List.of(1, 2)).list();
		Assert.assertEquals(actual, List.of(1, 1, 2));
	}

}
