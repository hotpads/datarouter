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
package io.datarouter.util.number;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NumberFormatterTests{

	@Test
	public void testFormat(){
		double input = 1234567890.1234567890;
		Assert.assertEquals(NumberFormatter.format(input, "$", "", 2), "$1,234,567,890.12");
		Assert.assertEquals(NumberFormatter.format(input, "", "", 0), "1,234,567,890");
	}

	@Test
	public void testAddCommas(){
		Short nullShort = null;
		Assert.assertNull(NumberFormatter.addCommas(nullShort));
		Assert.assertEquals(NumberFormatter.addCommas(0), "0");
		Assert.assertEquals(NumberFormatter.addCommas(Short.MIN_VALUE), "-32,768");
		Assert.assertEquals(NumberFormatter.addCommas(Short.MAX_VALUE), "32,767");

		Integer nullInteger = null;
		Assert.assertNull(NumberFormatter.addCommas(nullInteger));
		Assert.assertEquals(NumberFormatter.addCommas(0), "0");
		Assert.assertEquals(NumberFormatter.addCommas(Integer.MIN_VALUE), "-2,147,483,648");
		Assert.assertEquals(NumberFormatter.addCommas(Integer.MAX_VALUE), "2,147,483,647");

		Long nullLong = null;
		Assert.assertNull(NumberFormatter.addCommas(nullLong));
		Assert.assertEquals(NumberFormatter.addCommas(0L), "0");
		Assert.assertEquals(NumberFormatter.addCommas(Long.MIN_VALUE), "-9,223,372,036,854,775,808");
		Assert.assertEquals(NumberFormatter.addCommas(Long.MAX_VALUE), "9,223,372,036,854,775,807");

		// precision overflow
		Assert.assertEquals(NumberFormatter.addCommas(1234567890.1234567890), "1,234,567,890.1234567");
		Assert.assertEquals(NumberFormatter.addCommas(1234.1234567890), "1,234.123456789");
		Assert.assertEquals(NumberFormatter.addCommas(1234.123456789012), "1,234.123456789012");
	}

}
