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
package io.datarouter.bytes;

import org.testng.Assert;
import org.testng.annotations.Test;

public class KvStringTests{

	@Test
	public void testToString(){
		var kvString = new KvString()
				.add("a", 1, Number::toString)
				.add("b", "2")
				.addLazy("c", () -> 3, Number::toString)
				.addLazy("d", () -> "4");
		String expected = "a=1, b=2, c=3, d=4";
		Assert.assertEquals(kvString.toString(), expected);
	}

	@Test
	public void testNull(){
		var kvString = new KvString()
				.add("a", null, Number::toString)
				.add("b", null);
		String expected = "a=, b=";
		Assert.assertEquals(kvString.toString(), expected);
	}

}
