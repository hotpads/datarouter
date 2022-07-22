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
package io.datarouter.aws.sqs.blob.op;

import com.amazonaws.AbortedException;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;

import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.op.SqsBlobOp;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.util.concurrent.UncheckedInterruptedException;

public class SqsBlobAckOp extends SqsBlobOp<Void>{

	private final String handle;

	public SqsBlobAckOp(
			byte[] handle,
			Config config,
			SqsClientManager sqsClientManager,
			ClientId clientId,
			String queueUrl){
		super(sqsClientManager, clientId, config, queueUrl);
		this.handle = StringCodec.UTF_8.decode(handle);
	}

	@Override
	protected Void run(){
		var deleteRequest = new DeleteMessageRequest(queueUrl, handle);
		try{
			sqsClientManager.getAmazonSqs(clientId).deleteMessage(deleteRequest);
		}catch(AbortedException e){
			throw new UncheckedInterruptedException("", e);
		}
		return null;
	}

}
