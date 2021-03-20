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

import org.testng.Assert;
import org.testng.annotations.Test;

public class ReverseListScannerTests{

	@Test
	public void test(){
		Assert.assertEquals(ReverseListScanner.of(Java9.listOf()).list(), Java9.listOf());
		Assert.assertEquals(ReverseListScanner.of(Java9.listOf(1)).list(), Java9.listOf(1));
		Assert.assertEquals(ReverseListScanner.of(Java9.listOf(1, 2)).list(), Java9.listOf(2, 1));
	}

	@Test
	public void testNulls(){
		Assert.assertEquals(ReverseListScanner.of(Arrays.asList(1, null)).list(), Arrays.asList(null, 1));
		Assert.assertEquals(ReverseListScanner.of(Arrays.asList(1, null, 3)).list(), Arrays.asList(3, null, 1));
	}

}
