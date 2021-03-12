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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SplittingScannerTests{

	private static final Function<String,String> FIRST = str -> str == null ? null : str.substring(0, 1);

	@Test
	public void testEmpty(){
		boolean hasAny = Scanner.of(new ArrayList<String>())
				.splitBy(FIRST)
				.hasAny();
		Assert.assertFalse(hasAny);
	}

	@Test
	public void testNormal(){
		List<List<String>> result = Scanner.of("a1", "a2", "b1", null, "b2", "c1", "a3", "a4")
				.splitBy(FIRST)
				.map(Scanner::list)
				.list();
		Iterator<List<String>> iter = result.iterator();
		Assert.assertEquals(iter.next(), Java9.listOf("a1", "a2"));
		Assert.assertEquals(iter.next(), Java9.listOf("b1"));
		Assert.assertEquals(iter.next(), Arrays.asList((String)null));
		Assert.assertEquals(iter.next(), Java9.listOf("b2"));
		Assert.assertEquals(iter.next(), Java9.listOf("c1"));
		Assert.assertEquals(iter.next(), Java9.listOf("a3", "a4"));
		Assert.assertFalse(iter.hasNext());
	}

	@Test
	public void testUndrainedInnerScanner(){
		List<Scanner<String>> result = Scanner.of("a1", "a2", "b1", "b2", "c1")
				.splitBy(FIRST)
				.list();
		//We didn't advance the sub-scanners but should still get 3 (terminated) scanners.
		Assert.assertEquals(result.size(), 3);
	}

}
