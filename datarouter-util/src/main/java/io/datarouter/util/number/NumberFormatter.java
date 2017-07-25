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

import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NumberFormatter{

	public static String format(Number number, int numFractionDigits){
		return format(number, "", "", numFractionDigits, true);
	}

	public static String format(Number number, String prefix, String suffix, int numFractionDigits){
		return format(number, prefix, suffix, numFractionDigits, true);
	}

	public static String format(Number number, String prefix, String suffix, int numFractionDigits, boolean grouping){
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(numFractionDigits);
		df.setMinimumFractionDigits(numFractionDigits);
		df.setRoundingMode(RoundingMode.HALF_UP);
		df.setGroupingUsed(grouping);
		df.setPositivePrefix(prefix);
		df.setNegativePrefix(prefix + "-");
		df.setPositiveSuffix(suffix);
		df.setNegativeSuffix(suffix);
		return df.format(number);
	}

	public static String addCommas(final Number value){
		if(value == null){
			return null;
		}
		//biggest is 19 digits
		return new DecimalFormat("###,###,###,###,###,###,###,###.#####################").format(value);
	}

	public static class Tests{
		@Test public void testFormat(){
			double input = 1234567890.1234567890;
			Assert.assertEquals(format(input,"$","",2), "$1,234,567,890.12");
			Assert.assertEquals(format(input,"","",0), "1,234,567,890");
		}
		@Test public void testAddCommas(){
			Short nullShort = null;
			Assert.assertEquals(addCommas(nullShort), null);
			Assert.assertEquals(addCommas(0), "0");
			Assert.assertEquals(addCommas(Short.MIN_VALUE), "-32,768");
			Assert.assertEquals(addCommas(Short.MAX_VALUE), "32,767");

			Integer nullInteger = null;
			Assert.assertEquals(addCommas(nullInteger), null);
			Assert.assertEquals(addCommas(0), "0");
			Assert.assertEquals(addCommas(Integer.MIN_VALUE), "-2,147,483,648");
			Assert.assertEquals(addCommas(Integer.MAX_VALUE), "2,147,483,647");

			Long nullLong = null;
			Assert.assertEquals(addCommas(nullLong), null);
			Assert.assertEquals(addCommas(0L), "0");
			Assert.assertEquals(addCommas(Long.MIN_VALUE), "-9,223,372,036,854,775,808");
			Assert.assertEquals(addCommas(Long.MAX_VALUE), "9,223,372,036,854,775,807");

//			System.out.println(addCommas(Double.MAX_VALUE));
//			System.out.println(addCommas(1234567890.1234567890));
			Assert.assertEquals(addCommas(1234567890.1234567890), "1,234,567,890.1234567");//precision overflow
			Assert.assertEquals(addCommas(1234.1234567890), "1,234.123456789");
//			System.out.println(addCommas(1234.1234567890123456789));
			Assert.assertEquals(addCommas(1234.123456789012), "1,234.123456789012");
		}
	}
}
