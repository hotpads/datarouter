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
package io.datarouter.instrumentation.gauge;

import java.util.ArrayList;

public class Gauges{

	private static final ArrayList<MetricCollector> COLLECTORS = new ArrayList<>();
	private static final ArrayList<MetricCollector> RAW_COLLECTORS = new ArrayList<>();

	/*----------- admin -------------*/

	public static void addCollector(MetricCollector collector){
		COLLECTORS.add(collector);
	}

	public static void addRawCollector(MetricCollector collector){
		RAW_COLLECTORS.add(collector);
	}

	/*------------ client -----------*/

	public static void saveWithPercentiles(String key, long value){
		save(key, value);
		RAW_COLLECTORS.forEach(collector -> collector.save(key, value));
	}

	public static void save(String key, long value){
		COLLECTORS.forEach(collector -> collector.save(key, value));
	}

}
