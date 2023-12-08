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
package io.datarouter.client.gcp.pubsub.op.blob;

import java.util.concurrent.Callable;

import com.google.pubsub.v1.TopicName;

import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.client.gcp.pubsub.config.DatarouterGcpPubsubSettingsRoot;
import io.datarouter.client.gcp.pubsub.node.GcpPubsubBlobNode;
import io.datarouter.storage.client.ClientId;

public abstract class GcpPubsubBlobOp<V> implements Callable<V>{

	protected final String subscriberId;
	protected final TopicName topicId;
	protected final GcpPubsubClientManager clientManager;
	protected final DatarouterGcpPubsubSettingsRoot settingRoot;
	protected final ClientId clientId;

	private final int maxDataSize;

	public GcpPubsubBlobOp(
			GcpPubsubBlobNode<?> node,
			GcpPubsubClientManager clientManager,
			DatarouterGcpPubsubSettingsRoot settingRoot,
			ClientId clientId){
		this.subscriberId = node.getTopicAndSubscriptionName().get().subscription();
		this.topicId = node.getTopicAndSubscriptionName().get().topic();
		this.clientManager = clientManager;
		this.settingRoot = settingRoot;
		this.clientId = clientId;
		this.maxDataSize = node.getMaxRawDataSize();
	}

	@Override
	public V call(){
		return run();
	}

	protected abstract V run();

	protected boolean isPutRequestTooBig(byte[] data){
		return data.length > maxDataSize;
	}

}
