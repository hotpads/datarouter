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

public class BatchingScannerTests{

	@Test
	public void testEmptyInputScanner(){
		Scanner<List<Integer>> batchingScanner = Scanner.<Integer>empty().batch(3);
		Assert.assertFalse(batchingScanner.advance());
	}

	@Test
	public void testPartialBatch(){
		Scanner<List<Integer>> batchingScanner = Scanner.of(0, 1).batch(3);
		Assert.assertTrue(batchingScanner.advance());
		List<Integer> batch = batchingScanner.current();
		Assert.assertEquals(batch.size(), 2);
		Assert.assertEquals(batch.get(0).intValue(), 0);
		Assert.assertEquals(batch.get(1).intValue(), 1);
		Assert.assertFalse(batchingScanner.advance());
	}

	@Test
	public void testMultipleBatches(){
		Scanner<List<Integer>> batchingScanner = Scanner.of(0, 1, 2, 3, 4, 5, 6, 7).batch(3);
		List<List<Integer>> batches = batchingScanner.list();
		Assert.assertEquals(batches.size(), 3);
		Assert.assertEquals(batches.get(0).get(2).intValue(), 2);
		Assert.assertEquals(batches.get(1).get(1).intValue(), 4);
		Assert.assertEquals(batches.get(2).size(), 2);
	}

}
