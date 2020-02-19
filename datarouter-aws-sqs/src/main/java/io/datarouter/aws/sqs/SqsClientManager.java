/**
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.QueueAttributeName;

import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;

@Singleton
public class SqsClientManager extends BaseClientManager{

	private static final List<String> ALL_ATTRIBUTE_AS_LIST = Collections.singletonList(QueueAttributeName.All.name());

	@Inject
	private AmazonSqsHolder amazonSqsHolder;

	@Override
	public void shutdown(ClientId clientId){
		amazonSqsHolder.get(clientId).shutdown();
	}

	@Override
	protected void safeInitClient(ClientId clientId){
		amazonSqsHolder.registerClient(clientId);
	}

	public AmazonSQS getAmazonSqs(ClientId clientId){
		initClient(clientId);
		return amazonSqsHolder.get(clientId);
	}

	public Map<String,String> getQueueAttributes(ClientId clientId, String sqsQueueUrl, List<String> attributes){
		initClient(clientId);
		return amazonSqsHolder.get(clientId).getQueueAttributes(sqsQueueUrl, attributes).getAttributes();
	}

	public Map<String,String> getAllQueueAttributes(ClientId clientId, String sqsQueueUrl){
		return getQueueAttributes(clientId, sqsQueueUrl, ALL_ATTRIBUTE_AS_LIST);
	}

}
