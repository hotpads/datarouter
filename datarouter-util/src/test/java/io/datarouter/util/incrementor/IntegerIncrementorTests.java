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
package io.datarouter.util.incrementor;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class IntegerIncrementorTests{

	@Test
	public void testFromInclusive(){
		List<Integer> actual = IntegerIncrementor.fromInclusive(2).limit(3).list();
		List<Integer> expected = Arrays.asList(2, 3, 4);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testFromInclusiveStep(){
		List<Integer> actual = IntegerIncrementor.fromInclusive(2).step(2).limit(3).list();
		List<Integer> expected = Arrays.asList(2, 4, 6);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testToExclusive(){
		List<Integer> actual = IntegerIncrementor.toExclusive(3).list();
		List<Integer> expected = Arrays.asList(0, 1, 2);
		Assert.assertEquals(actual, expected);
	}

}
