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

public class NumberFormatter{

	public static String format(Number number, int numFractionDigits){
		return format(number, "", "", numFractionDigits, numFractionDigits, true);
	}

	public static String format(Number number, String prefix, String suffix, int numFractionDigits){
		return format(number, prefix, suffix, numFractionDigits, numFractionDigits, true);
	}

	public static String format(Number number, String prefix, String suffix, int minFractionDigits,
			int maxFractionDigits, boolean grouping){
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(maxFractionDigits);
		df.setMinimumFractionDigits(minFractionDigits);
		df.setRoundingMode(RoundingMode.HALF_UP);
		df.setGroupingUsed(grouping);
		df.setPositivePrefix(prefix);
		df.setNegativePrefix(prefix + "-");
		df.setPositiveSuffix(suffix);
		df.setNegativeSuffix(suffix);
		return df.format(number);
	}

	public static String addCommas(Number value){
		if(value == null){
			return null;
		}
		// biggest is 19 digits
		return new DecimalFormat("###,###,###,###,###,###,###,###.#####################").format(value);
	}

}
