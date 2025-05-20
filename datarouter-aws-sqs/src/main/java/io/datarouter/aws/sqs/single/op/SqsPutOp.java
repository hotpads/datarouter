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

import java.util.List;

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
import software.amazon.awssdk.core.exception.AbortedException;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class SqsPutOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SqsOp<PK,D,F,Void>{

	private final D databean;
	private final SqsClientManager sqsClientManager;
	private final ClientId clientId;

	public SqsPutOp(
			D databean,
			Config config,
			BaseSqsNode<PK,D,F> sqsNode,
			SqsClientManager sqsClientManager,
			ClientId clientId){
		super(config, sqsNode);
		this.databean = databean;
		this.sqsClientManager = sqsClientManager;
		this.clientId = clientId;
	}

	@Override
	protected Void run(){
		FieldGeneratorTool.generateAndSetValueForFieldIfNecessary(fieldInfo, databean);
		String encodedDatabean = codec.toString(databean, fielder);
		if(StringCodec.UTF_8.encode(encodedDatabean).length > CommonFieldSizes.MAX_SQS_SIZE){
			throw new SqsDataTooLargeException(List.of(encodedDatabean));
		}
		var request = SendMessageRequest.builder()
				.queueUrl(queueUrl)
				.messageBody(encodedDatabean)
				.build();
		try{
			sqsClientManager.getAmazonSqs(clientId).sendMessage(request);
		}catch(AbortedException e){
			throw new UncheckedInterruptedException("", e);
		}
		return null;
	}

}
