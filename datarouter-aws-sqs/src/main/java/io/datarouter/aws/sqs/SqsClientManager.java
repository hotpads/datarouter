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

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.amazonaws.http.IdleConnectionReaper;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.QueueAttributeName;

import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;

@Singleton
public class SqsClientManager extends BaseClientManager{

	@Inject
	private AmazonSqsHolder amazonSqsHolder;

	@Override
	public void shutdown(ClientId clientId){
		amazonSqsHolder.get(clientId).shutdown();
		IdleConnectionReaper.shutdown();
	}

	@Override
	protected void safeInitClient(ClientId clientId){
		amazonSqsHolder.registerClient(clientId);
	}

	public AmazonSQS getAmazonSqs(ClientId clientId){
		initClient(clientId);
		return amazonSqsHolder.get(clientId);
	}

	public String getQueueAttribute(ClientId clientId, String queueUrl, QueueAttributeName attributeName){
		return getQueueAttributes(clientId, queueUrl, List.of(attributeName.name())).get(attributeName.name());
	}

	public Map<String,String> getAllQueueAttributes(ClientId clientId, String sqsQueueUrl){
		return getQueueAttributes(clientId, sqsQueueUrl, List.of(QueueAttributeName.All.name()));
	}

	public Map<String,String> getQueueAttributes(ClientId clientId, String queueUrl, List<String> attributes){
		return getAmazonSqs(clientId).getQueueAttributes(queueUrl, attributes).getAttributes();
	}

	public void updateAttr(ClientId clientId, String queueUrl, QueueAttributeName key, Object value){
		Map<String,String> attributes = Map.of(key.name(), String.valueOf(value));
		getAmazonSqs(clientId).setQueueAttributes(queueUrl, attributes);
	}

}
