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
package io.datarouter.model.util;

import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;

public class FractionalSecondTool{

	private static final int TOTAL_NUM_FRACTIONAL_SECONDS = 9;

	@SuppressWarnings("unchecked")
	public static <R extends Temporal> R truncate(R value, int numFractionalSeconds){
		if(value == null){
			return null;
		}
		int divideBy = (int) Math.pow(10, TOTAL_NUM_FRACTIONAL_SECONDS - numFractionalSeconds);
		if(divideBy < 1){
			throw new RuntimeException("numFractionalSeconds is greater or equal to 9");
		}
		int numNanoSeconds = value.get(ChronoField.NANO_OF_SECOND) / divideBy * divideBy;
		return (R)value.with(ChronoField.NANO_OF_SECOND, numNanoSeconds);
	}

}
