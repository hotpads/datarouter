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
package io.datarouter.util.number;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NumberToolTests{

	@Test
	public void testIsNullOrZero(){
		Byte byt = 0;
		Short shrt = 0;
		Integer intgr = 0;
		Long lng = 0L;
		Float flt = 0.0f;
		Double dub = 0.0;
		Assert.assertTrue(NumberTool.isNullOrZero(byt));
		Assert.assertTrue(NumberTool.isNullOrZero(shrt));
		Assert.assertTrue(NumberTool.isNullOrZero(intgr));
		Assert.assertTrue(NumberTool.isNullOrZero(lng));
		Assert.assertTrue(NumberTool.isNullOrZero(flt));
		Assert.assertTrue(NumberTool.isNullOrZero(dub));
	}

	@Test
	public void testParseIntegerFromNumberString(){
		Assert.assertEquals(NumberTool.parseIntegerFromNumberString("5.5", null), Integer.valueOf(5));
		Assert.assertEquals(NumberTool.parseIntegerFromNumberString("5.0", null), Integer.valueOf(5));
		Assert.assertEquals(NumberTool.parseIntegerFromNumberString("5.9", null), Integer.valueOf(5));
		Assert.assertEquals(NumberTool.parseIntegerFromNumberString("-9", null), Integer.valueOf(-9));
		Assert.assertEquals(NumberTool.parseIntegerFromNumberString("-9", -9), Integer.valueOf(-9));
		Assert.assertEquals(NumberTool.parseIntegerFromNumberString("5-9", null), null);
		Assert.assertEquals(NumberTool.parseIntegerFromNumberString(null, null), null);
		Assert.assertEquals(NumberTool.parseIntegerFromNumberString("banana", null), null);
		Assert.assertEquals(NumberTool.parseIntegerFromNumberString("banana", 2), Integer.valueOf(2));
		Assert.assertEquals(NumberTool.parseIntegerFromNumberString("banana", 2), Integer.valueOf(2));
		Assert.assertEquals(NumberTool.parseIntegerFromNumberString("2", 3), Integer.valueOf(2));
		Assert.assertEquals(NumberTool.parseIntegerFromNumberString(Integer.MAX_VALUE + "", null),
				Integer.valueOf(Integer.MAX_VALUE));
		Assert.assertEquals(NumberTool.parseIntegerFromNumberString(Integer.MIN_VALUE + "", null),
				Integer.valueOf(Integer.MIN_VALUE));

		Assert.assertEquals(NumberTool.parseIntegerFromNumberString("$400,000", null, true), Integer.valueOf(400000));
		Assert.assertNull(NumberTool.parseIntegerFromNumberString("$400,000", null, false));
	}

	@Test
	public void testCachedIntegers(){
		Integer foo0 = 1000;
		Integer bar0 = 1000;
		Assert.assertTrue(foo0 <= bar0);
		Assert.assertTrue(foo0 >= bar0);
		Assert.assertFalse(foo0 == bar0);

		Integer foo1 = 42;
		Integer bar1 = 42;
		Assert.assertTrue(foo1 <= bar1);
		Assert.assertTrue(foo1 >= bar1);
		Assert.assertTrue(foo1 == bar1);

		Integer foo2 = 1000;
		int bar2 = 1000;
		Assert.assertTrue(foo2 <= bar2);
		Assert.assertTrue(foo2 >= bar2);
		Assert.assertTrue(foo2 == bar2);
	}

	@Test
	public void testCastLongToInt(){
		long maxIntValue = Integer.MAX_VALUE;
		long maxIntValuePlusOne = maxIntValue + 1L;
		long maxIntValueMinusOne = maxIntValue - 1L;
		Assert.assertEquals(NumberTool.limitLongToIntRange(maxIntValue), Integer.MAX_VALUE);
		Assert.assertEquals(NumberTool.limitLongToIntRange(maxIntValuePlusOne), Integer.MAX_VALUE);
		Assert.assertEquals(NumberTool.limitLongToIntRange(maxIntValueMinusOne), Integer.MAX_VALUE - 1);

		long minIntValue = Integer.MIN_VALUE;
		long minIntValueMinusOne = minIntValue - 1L;
		long minIntValuePlusOne = minIntValue + 1L;
		Assert.assertEquals(NumberTool.limitLongToIntRange(minIntValue), Integer.MIN_VALUE);
		Assert.assertEquals(NumberTool.limitLongToIntRange(minIntValueMinusOne), Integer.MIN_VALUE);
		Assert.assertEquals(NumberTool.limitLongToIntRange(minIntValuePlusOne), Integer.MIN_VALUE + 1);
	}

}