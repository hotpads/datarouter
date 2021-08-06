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

import java.util.Optional;

import io.datarouter.util.string.StringTool;

public class NumberTool{

	/*------------------------- is this or that methods ---------------------*/

	public static boolean isEmpty(Number number){
		return isNullOrZero(number);
	}

	public static boolean notEmpty(Number number){
		return !isEmpty(number);
	}

	// careful, this method is fragile and not even sure if works with BigInteger stuff now
	public static boolean isNullOrZero(Number number){
		return number == null || number.equals(0L) || number.equals(0F) || number.equals(0D) || number.intValue() == 0;
	}

	public static Long max(Long n1, Long n2){
		if(n1 == null){
			return n2;
		}
		if(n2 == null){
			return n1;
		}
		return Math.max(n1, n2);
	}

	public static final boolean isPositive(Integer in){
		if(in == null){
			return false;
		}
		if(in <= 0){
			return false;
		}
		return true;
	}

	/*------------------------- numeric null safe ---------------------------*/

	public static Integer nullSafe(Integer in){
		if(in == null){
			return 0;
		}
		return in;
	}

	public static Long nullSafeLong(Long in, Long defaultValue){
		if(in == null){
			return defaultValue;
		}
		return in;
	}

	public static Long longValue(Number number){
		if(number == null){
			return null;
		}
		return number.longValue();
	}

	/*------------------------- parsing -------------------------------------*/

	public static Double getDoubleNullSafe(String toDouble, Double alternate){
		return getDoubleNullSafe(toDouble, alternate, false);
	}

	public static Double getDoubleNullSafe(String toDouble, Double alternate, boolean filterInput){
		if(toDouble == null){
			return alternate;
		}
		if(filterInput){
			toDouble = StringTool.enforceNumeric(toDouble);
			if(toDouble == null){
				return alternate;
			}
		}
		try{
			return Double.valueOf(toDouble);
		}catch(NumberFormatException e){
			return alternate;
		}
	}

	// e.g. For "5.3", it will return 5
	public static Integer parseIntegerFromNumberString(String toInteger, Integer alternate){
		return parseIntegerFromNumberString(toInteger, alternate, false);
	}

	// e.g. For "5.3", it will return 5
	public static Integer parseIntegerFromNumberString(String toInteger, Integer alternate, boolean filterInput){
		Double dub = getDoubleNullSafe(toInteger, null, filterInput);
		if(dub == null){
			return alternate;
		}
		return dub.intValue();
	}

	public static Long getLongNullSafe(String toLong, Long alternate){
		if(toLong == null){
			return alternate;
		}
		try{
			return Long.valueOf(toLong);
		}catch(NumberFormatException e){
			return alternate;
		}
	}

	// Optional variants
	public static Optional<Double> parseDouble(String toDouble){
		return Optional.ofNullable(getDoubleNullSafe(toDouble, null));
	}

	public static Optional<Long> parseLong(String toLong){
		return Optional.ofNullable(getLongNullSafe(toLong, null));
	}

	public static Optional<Integer> parseInteger(String toInteger){
		return Optional.ofNullable(parseIntegerFromNumberString(toInteger, null));
	}

	/**
	 * Casts a long to an int, with overflows resulting in {@link Integer#MAX_VALUE} or {@link Integer#MIN_VALUE}
	 */
	public static int limitLongToIntRange(long in){
		if(in >= 0){
			return in > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)in;
		}
		return in < Integer.MIN_VALUE ? Integer.MIN_VALUE : (int)in;
	}

}
