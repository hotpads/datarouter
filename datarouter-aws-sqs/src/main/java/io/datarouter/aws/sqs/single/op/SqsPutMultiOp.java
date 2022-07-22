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
package io.datarouter.aws.sqs.single.op;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.amazonaws.AbortedException;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.SqsDataTooLargeException;
import io.datarouter.aws.sqs.op.SqsOp;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.FieldGeneratorTool;
import io.datarouter.util.concurrent.UncheckedInterruptedException;

public class SqsPutMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SqsOp<PK,D,F,Void>{

	private final Collection<D> databeans;
	private final SqsClientManager sqsClientManager;
	private final ClientId clientId;

	public SqsPutMultiOp(
			Collection<D> databeans,
			Config config,
			BaseSqsNode<PK,D,F> sqsNode,
			SqsClientManager sqsClientManager,
			ClientId clientId){
		super(config, sqsNode);
		this.databeans = databeans;
		this.sqsClientManager = sqsClientManager;
		this.clientId = clientId;
	}

	@Override
	protected Void run(){
		List<SendMessageBatchRequestEntry> entries = new ArrayList<>(BaseSqsNode.MAX_MESSAGES_PER_BATCH);
		List<String> rejectedDatabeans = new ArrayList<>();
		int currentPayloadSize = 0;
		for(D databean : databeans){
			FieldGeneratorTool.generateAndSetValueForFieldIfNecessary(fieldInfo, databean);
			String databeanAsString = codec.toString(databean, fielder);
			int encodedDatabeanSize = StringCodec.UTF_8.encode(databeanAsString).length;
			if(encodedDatabeanSize > CommonFieldSizes.MAX_SQS_SIZE){
				rejectedDatabeans.add(databeanAsString);
				continue;
			}
			if(currentPayloadSize + encodedDatabeanSize > CommonFieldSizes.MAX_SQS_SIZE
					|| entries.size() >= BaseSqsNode.MAX_MESSAGES_PER_BATCH){
				putBatch(entries);
				entries = new ArrayList<>();
				currentPayloadSize = 0;
			}
			entries.add(new SendMessageBatchRequestEntry(UUID.randomUUID().toString(), databeanAsString));
			currentPayloadSize += encodedDatabeanSize;
		}
		if(entries.size() > 0){
			putBatch(entries);
		}
		if(rejectedDatabeans.size() > 0){
			throw new SqsDataTooLargeException(rejectedDatabeans);
		}
		return null;
	}

	private void putBatch(List<SendMessageBatchRequestEntry> entries){
		var request = new SendMessageBatchRequest(queueUrl, entries);
		try{
			sqsClientManager.getAmazonSqs(clientId).sendMessageBatch(request);
		}catch(AbortedException e){
			throw new UncheckedInterruptedException("", e);
		}
	}

}
