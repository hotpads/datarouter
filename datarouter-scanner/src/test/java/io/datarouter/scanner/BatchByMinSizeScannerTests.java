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

import io.datarouter.scanner.BatchByMinSizeScanner.ScannerMinSizeBatch;

public class BatchByMinSizeScannerTests{

	@Test
	public void testEmpty(){
		List<String> inputs = List.of();
		List<List<String>> expected = List.of();
		List<List<String>> actual = Scanner.of(inputs)
				.batchByMinSize(3, String::length)
				.list();
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testOne(){
		List<String> inputs = List.of("a");
		List<List<String>> expected = List.of(List.of("a"));
		List<List<String>> actual = Scanner.of(inputs)
				.batchByMinSize(3, String::length)
				.list();
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testMultiple(){
		List<String> inputs = List.of("a", "bb", "c", "ddddd", "ee", "ff", "g");
		List<ScannerMinSizeBatch<String>> expected = List.of(
				new ScannerMinSizeBatch<>(List.of("a", "bb", "c"), 4),
				new ScannerMinSizeBatch<>(List.of("ddddd"), 5),
				new ScannerMinSizeBatch<>(List.of("ee", "ff"), 4),
				new ScannerMinSizeBatch<>(List.of("g"), 1));
		List<ScannerMinSizeBatch<String>> actual = Scanner.of(inputs)
				.batchByMinSizeWithStats(4, String::length)
				.list();
		Assert.assertEquals(actual, expected);
	}

}
