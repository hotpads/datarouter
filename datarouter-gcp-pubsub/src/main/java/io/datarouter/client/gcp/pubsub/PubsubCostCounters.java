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
package io.datarouter.client.gcp.pubsub;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.ReceivedMessage;

import io.datarouter.instrumentation.cost.CostCounters;

public class PubsubCostCounters{

	// $40/TiB -> 25B/nano
	private static final long BYTES_PER_NANO = 25;

	public static void countMessage(ReceivedMessage receivedMessage){
		countMessage(receivedMessage.getMessage());
	}

	public static void countMessage(PubsubMessage message){
		long roundedBytes = Math.max(1_024, messageSize(message));
		long nanos = roundedBytes / BYTES_PER_NANO;
		CostCounters.nanos("data", "messaging", "pubsub", "request", nanos);
	}

	private static int messageSize(PubsubMessage message){
		var bytes = new AtomicInteger();
		bytes.addAndGet(message.getData().size());
		message.getAttributesMap().forEach((key, value) -> {
			bytes.addAndGet(key.length());
			bytes.addAndGet(value.length());
		});
		bytes.addAndGet(20);// timestamp
		bytes.addAndGet(message.getMessageIdBytes().size());
		return bytes.get();
	}

}
