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
package io.datarouter.aws.sqs.single.op;

import java.util.Arrays;

import com.amazonaws.services.sqs.model.SendMessageRequest;

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.SqsDataTooLargeException;
import io.datarouter.aws.sqs.op.SqsOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.FieldGeneratorTool;
import io.datarouter.util.bytes.StringByteTool;

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
		if(StringByteTool.getUtf8Bytes(encodedDatabean).length > BaseSqsNode.MAX_BYTES_PER_MESSAGE){
			throw new SqsDataTooLargeException(Arrays.asList(encodedDatabean));
		}
		var request = new SendMessageRequest(queueUrl, encodedDatabean);
		sqsClientManager.getAmazonSqs(clientId).sendMessage(request);
		return null;
	}

}
