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
package io.datarouter.storage.util;

import io.datarouter.instrumentation.metric.Metrics;

public class VacuumMetrics{

	//prefix
	private static final String PREFIX = "Vacuum";
	//suffix
	private static final String CONSIDERED = "considered";
	private static final String DELETED = "deleted";

	public static void considered(String name, long count){
		count(count, PREFIX, name, CONSIDERED);
	}

	public static void deleted(String name, long count){
		count(count, PREFIX, name, DELETED);
	}

	private static void count(long count, String... tokens){
		String name = String.join(" ", tokens);
		Metrics.count(name, count);
	}
}
