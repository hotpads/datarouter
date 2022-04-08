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

public class CollatingScannerTests{

	@Test
	public void test(){
		Scanner<List<Integer>> batches = Scanner.of(
				List.of(),
				List.of(2, 7, 8),
				List.of(1, 2, 7),
				List.of(),
				List.of(),
				List.of(0, 9));
		List<Integer> actual = batches
				.collate(Scanner::of)
				.list();
		List<Integer> expected = List.of(0, 1, 2, 2, 7, 7, 8, 9);
		Assert.assertEquals(actual, expected);
	}

}
