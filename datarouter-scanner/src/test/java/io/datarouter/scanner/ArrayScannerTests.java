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

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ArrayScannerTests{

	@Test
	public void test(){
		Assert.assertEquals(Scanner.of().list(), List.of());
		Assert.assertEquals(Scanner.of(1).list(), List.of(1));
		Assert.assertEquals(Scanner.of(1, 2).list(), List.of(1, 2));
	}

	@Test
	public void testNulls(){
		Assert.assertEquals(Scanner.of(1, null).list(), Arrays.asList(1, null));
		Assert.assertEquals(Scanner.of(1, null, 3).list(), Arrays.asList(1, null, 3));
	}

}
