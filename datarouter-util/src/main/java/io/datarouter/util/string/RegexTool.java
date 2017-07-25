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

import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RegexTool{

	public static final Pattern BACKSLASH_PATTERN = Pattern.compile("\\\\");
	public static final Pattern APOSTROPHE_PATTERN = Pattern.compile("'");

	public static String makeCharacterClassFromRange(int bottom, int top, boolean brackets){
		String characterClass = "";
		for(int c = bottom; c <= top; c++){
			characterClass += "\\u" + StringTool.pad(Integer.toHexString(c), '0', 4);
		}
		return (brackets ? "[" : "") + characterClass + (brackets ? "]" : "");
	}

	public static class Tests{
		@Test
		public void testMakeCharacterClassFromRange(){
			Assert.assertEquals(makeCharacterClassFromRange(1, 0, true), "[]");
			Assert.assertEquals(makeCharacterClassFromRange(0, 2, true), "[\\u0000\\u0001\\u0002]");
			Assert.assertEquals("01a,2  .3 smells 4".replaceAll(makeCharacterClassFromRange(49, 51,
					true), ""), "0a,  . smells 4");
		}
	}
}
