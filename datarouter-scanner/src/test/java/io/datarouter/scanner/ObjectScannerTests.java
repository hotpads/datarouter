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

import org.testng.Assert;
import org.testng.annotations.Test;

public class ObjectScannerTests{

	@Test
	public void test(){
		Assert.assertEquals(Scanner.of().list(), List.of());
		Assert.assertEquals(Scanner.of(1).list(), List.of(1));
	}

	@Test
	public void testOveradvance(){
		Scanner<Integer> scanner = Scanner.of(1);
		Assert.assertTrue(scanner.advance());
		Assert.assertEquals(scanner.current(), Integer.valueOf(1));
		Assert.assertFalse(scanner.advance());
	}

}
