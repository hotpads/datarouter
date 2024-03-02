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
package io.datarouter.instrumentation.metric;

import java.util.ArrayList;
import java.util.List;

public class Metrics{

	private static final List<MetricCollector> COLLECTORS = new ArrayList<>();

	public static synchronized void registerCollector(MetricCollector collector){
		COLLECTORS.add(collector);
	}

	public static void count(String key){
		count(key, 1);
	}

	public static void count(String key, long value){
		COLLECTORS.forEach(collector -> collector.count(key, value));
	}

	public static void measure(String key, long value){
		COLLECTORS.forEach(collector -> collector.measure(key, value, false));
	}

	public static void measureWithPercentiles(String key, long value){
		COLLECTORS.forEach(collector -> collector.measure(key, value, true));
	}

}
