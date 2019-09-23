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
package io.datarouter.util.string;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RegexToolTests{

	@Test
	public void testMakeCharacterClassFromRange(){
		Assert.assertEquals(RegexTool.makeCharacterClassFromRange(1, 0, true), "[]");
		Assert.assertEquals(RegexTool.makeCharacterClassFromRange(0, 2, true), "[\\u0000\\u0001\\u0002]");
		Assert.assertEquals("01a,2  .3 smells 4".replaceAll(RegexTool.makeCharacterClassFromRange(49, 51, true), ""),
				"0a,  . smells 4");
	}

}
