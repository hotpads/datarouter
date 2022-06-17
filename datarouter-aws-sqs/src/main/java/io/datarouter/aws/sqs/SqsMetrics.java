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
package io.datarouter.aws.sqs;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.metric.Gauges;
import io.datarouter.storage.util.DatarouterCounters;

@Singleton
public class SqsMetrics{

	@Inject
	private Gauges gauges;

	public void saveSqsQueueLength(String key, long queueLength){
		gauges.save(DatarouterCounters.PREFIX + " sqs queue length " + key, queueLength);
	}

	public void saveUnackedMessageAge(String key, long oldestUnackedMessageAgeS){
		gauges.save(DatarouterCounters.PREFIX + " sqs oldestMessageAgeS " + key, oldestUnackedMessageAgeS);
		gauges.save(DatarouterCounters.PREFIX + " sqs oldestMessageAgeM " + key, oldestUnackedMessageAgeS / 60);
	}

}
