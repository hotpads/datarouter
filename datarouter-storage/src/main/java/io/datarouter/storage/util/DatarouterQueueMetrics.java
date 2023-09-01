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

import io.datarouter.instrumentation.gauge.Gauges;

public class DatarouterQueueMetrics{

	public static final String QUEUE_LENGTH = "queue length";
	public static final String OLDEST_MESSAGE_AGE_S = "oldestMessageAgeS";
	public static final String OLDEST_MESSAGE_AGE_M = "oldestMessageAgeM";

	public static String makeNameForOldestMessageAgeS(String clientTypeName, String queueName){
		return makeMetricName(clientTypeName, OLDEST_MESSAGE_AGE_S, queueName);
	}

	public static String makeNameForQueueLength(String clientTypeName, String queueName){
		return makeMetricName(clientTypeName, QUEUE_LENGTH, queueName);
	}

	private static String makeMetricName(String clientTypeName, String key, String queueName){
		return DatarouterCounters.PREFIX + " " + clientTypeName + " " + key + " " + queueName;
	}

	public static void saveQueueLength(String key, long queueLength, String clientTypeName){
		Gauges.save(makeNameForQueueLength(clientTypeName, key), queueLength);
	}

	public static void saveOldestAckMessageAge(String key, long oldestUnackedMessageAgeS, String clientTypeName){
		Gauges.save(makeNameForOldestMessageAgeS(clientTypeName, key), oldestUnackedMessageAgeS);
	}

}
