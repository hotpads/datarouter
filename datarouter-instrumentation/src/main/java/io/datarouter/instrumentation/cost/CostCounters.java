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
package io.datarouter.instrumentation.cost;

import io.datarouter.instrumentation.count.Counters;

public class CostCounters{

	// Overall Cost counter prefix
	private static final String COST = "Cost";
	// For various input units, mostly for debugging
	private static final String INPUT = "input";
	// Nano-dollars
	private static final String NANOS = "nanos";
	// Total nano-dollars for viewing independently of per-type nanos.
	private static final String TOTAL_NANOS = "totalNanos";

	/*-------- input ---------*/

	public static void incInput(String type, long by){
		incCost(join(INPUT, type), by);
	}

	/*-------- nanos ---------*/

	public static void incNanos(String type, long by){
		incCost(join(NANOS, type), by);
		incCost(TOTAL_NANOS, by);
	}

	/*-------- cost ----------*/

	public static void incCost(String suffix, long by){
		Counters.inc(join(COST, suffix), by);
	}

	/*------- join ---------*/

	private static String join(String... tokens){
		return String.join(" ", tokens);
	}

}
