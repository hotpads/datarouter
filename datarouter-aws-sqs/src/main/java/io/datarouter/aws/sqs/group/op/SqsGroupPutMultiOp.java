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
package io.datarouter.aws.sqs.group.op;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.amazonaws.AbortedException;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.SqsDataTooLargeException;
import io.datarouter.aws.sqs.op.SqsOp;
import io.datarouter.bytes.StringByteTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.util.concurrent.UncheckedInterruptedException;

public class SqsGroupPutMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SqsOp<PK,D,F,Void>{

	private final Collection<D> databeans;
	private final int maxBoundedBytesPerMessage;
	private final SqsClientManager sqsClientManager;
	private final ClientId clientId;

	public SqsGroupPutMultiOp(
			Collection<D> databeans,
			Config config,
			BaseSqsNode<PK,D,F> sqsNode,
			SqsClientManager sqsClientManager,
			ClientId clientId){
		super(config, sqsNode);
		this.databeans = databeans;
		this.sqsClientManager = sqsClientManager;
		this.clientId = clientId;
		this.maxBoundedBytesPerMessage = BaseSqsNode.MAX_BYTES_PER_MESSAGE - codec.getCollectionPrefixBytes().length
				- codec.getCollectionSuffixBytes().length;
	}

	@Override
	protected Void run(){
		if(databeans.size() == 0){
			return null;
		}
		List<String> rejectedDatabeans = new ArrayList<>();
		List<byte[]> encodedDatabeans = new ArrayList<>();
		for(D databean : databeans){
			String databeanAsString = codec.toString(databean, fielder);
			byte[] databeanAsBytes = StringByteTool.getUtf8Bytes(databeanAsString);
			if(databeanAsBytes.length > maxBoundedBytesPerMessage){
				rejectedDatabeans.add(databeanAsString);
				continue;
			}
			encodedDatabeans.add(databeanAsBytes);
		}
		codec.makeGroups(encodedDatabeans, maxBoundedBytesPerMessage).forEach(this::flush);
		if(!rejectedDatabeans.isEmpty()){
			throw new SqsDataTooLargeException(rejectedDatabeans);
		}
		return null;
	}

	private void flush(List<byte[]> group){
		if(group.isEmpty()){
			return;
		}
		String stringGroup = codec.concatGroup(group);
		SendMessageRequest request = new SendMessageRequest(queueUrl, stringGroup);
		try{
			sqsClientManager.getAmazonSqs(clientId).sendMessage(request);
		}catch(AbortedException e){
			throw new UncheckedInterruptedException("", e);
		}
	}

}
