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
package io.datarouter.util;

import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

public class BooleanTool{

	private static final Set<String> TRUE_VALUES = new HashSet<>();
	private static final Set<String> FALSE_VALUES = new HashSet<>();

	static{
		TRUE_VALUES.add("true");
		TRUE_VALUES.add("1");
		TRUE_VALUES.add("t");
		TRUE_VALUES.add("yes");
		TRUE_VALUES.add("y");
		TRUE_VALUES.add("on");

		FALSE_VALUES.add("false");
		FALSE_VALUES.add("0");
		FALSE_VALUES.add("f");
		FALSE_VALUES.add("no");
		FALSE_VALUES.add("n");
		FALSE_VALUES.add("off");
	}

	public static boolean isTrue(String input){
		if(input == null){
			return false;
		}
		return TRUE_VALUES.contains(input.toLowerCase());
	}

	public static boolean isTrue(Boolean value){
		if(value == null){
			return false;
		}
		return value;
	}

	public static boolean isTrueOrNull(String input){
		if(input == null){
			return true;
		}
		return TRUE_VALUES.contains(input.toLowerCase());
	}

	public static boolean isFalse(String input){
		if(input == null){
			return false;
		}
		return FALSE_VALUES.contains(input.toLowerCase());
	}

	public static boolean isFalse(Boolean value){
		if(value == null){
			return false;
		}
		return !value;
	}

	public static boolean isFalseOrNull(Boolean value){
		if(value == null){
			return true;
		}
		return !value;
	}

	public static boolean isBoolean(String input){
		return isTrue(input) || isFalse(input);
	}

	public static boolean nullSafeSame(Boolean b1, Boolean b2){
		return b1 == null && b2 == null || isTrue(b1) && isTrue(b2) || isFalse(b1) && isFalse(b2);
	}

	public class DrBooleanToolTests{
		@Test
		public void testNullSafeSame(){
			Assert.assertEquals(nullSafeSame(null, null), true);
			Assert.assertEquals(nullSafeSame(true, true), true);
			Assert.assertEquals(nullSafeSame(false, false), true);
			Assert.assertEquals(nullSafeSame(true, null), false);
			Assert.assertEquals(nullSafeSame(null, true), false);
			Assert.assertEquals(nullSafeSame(false, null), false);
			Assert.assertEquals(nullSafeSame(null, false), false);
		}
	}
}
