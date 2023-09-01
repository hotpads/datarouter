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
package io.datarouter.nodewatch.util;

import io.datarouter.bytes.ByteLength;
import io.datarouter.instrumentation.cost.CostCounters;
import io.datarouter.util.time.DurationTool;

public class NodewatchCostCounters{

	private static final long BILLION = 1_000_000_000;

	// Call each minute
	public static void storage(String databaseType, double dollarsPerTiBPerMonth, ByteLength bytes){
		double dollarsPerMonth = bytes.toTiBDouble() * dollarsPerTiBPerMonth;
		double dollarsPerMinute = dollarsPerMonth / DurationTool.AVG_MONTH.toMinutes();
		double nanosPerMinute = dollarsPerMinute * BILLION;
		CostCounters.nanos("data", "database", databaseType, "storage", (long)nanosPerMinute);
	}

	// Call each minute
	public static void instance(String databaseType, double dollarsPerMonth){
		double dollarsPerMinute = dollarsPerMonth / DurationTool.AVG_MONTH.toMinutes();
		double nanosPerMinute = dollarsPerMinute * BILLION;
		CostCounters.nanos("data", "database", databaseType, "instance", (long)nanosPerMinute);
	}

}
