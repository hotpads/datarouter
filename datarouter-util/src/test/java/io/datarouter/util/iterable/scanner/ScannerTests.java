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
package io.datarouter.util.iterable.scanner;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ScannerTests{

	@Test
	public void testAdvanceBy(){
		Scanner<Integer> scanner = new TestScanner();
		Assert.assertTrue(scanner.advanceBy(10));
		Assert.assertEquals(scanner.getCurrent().intValue(), 10);
		Assert.assertFalse(scanner.advanceBy(1));
	}

	private static class TestScanner implements Scanner<Integer>{

		private Integer current = 0;

		@Override
		public Integer getCurrent(){
			return current;
		}

		@Override
		public boolean advance(){
			if(current == 10){
				return false;
			}
			current++;
			return true;
		}

	}

}
