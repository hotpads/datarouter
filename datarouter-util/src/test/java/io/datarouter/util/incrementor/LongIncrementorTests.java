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

public class LongIncrementorTests{

	@Test
	public void testFromInclusive(){
		List<Long> actual = LongIncrementor.fromInclusive(2).limit(3).list();
		List<Long> expected = Arrays.asList(2L, 3L, 4L);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testFromInclusiveStep(){
		List<Long> actual = LongIncrementor.fromInclusive(2).step(2).limit(3).list();
		List<Long> expected = Arrays.asList(2L, 4L, 6L);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testToExclusive(){
		List<Long> actual = LongIncrementor.toExclusive(3).list();
		List<Long> expected = Arrays.asList(0L, 1L, 2L);
		Assert.assertEquals(actual, expected);
	}

}
