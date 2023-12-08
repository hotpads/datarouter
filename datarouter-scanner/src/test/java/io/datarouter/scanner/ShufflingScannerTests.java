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
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ShufflingScannerTests{

	@Test
	public void testEmpty(){
		Assert.assertTrue(Scanner.empty().shuffle().isEmpty());
	}

	@Test
	public void testSingle(){
		Assert.assertEquals(Scanner.of("a").shuffle().findFirst().get(), "a");
	}

	@Test
	public void test(){
		int numItems = 100;
		List<Integer> input = Stream.iterate(0, i -> i + 1)
				.limit(numItems)
				.collect(WarnOnModifyList.deprecatedCollector());
		List<Integer> output = Scanner.of(input)
				.shuffle()
				.list();
		Assert.assertEquals(output.size(), numItems);
		Assert.assertTrue(output.containsAll(input));
		Assert.assertNotEquals(output, input);//tiny chance of flakiness if the shuffling has no effect
	}

}
