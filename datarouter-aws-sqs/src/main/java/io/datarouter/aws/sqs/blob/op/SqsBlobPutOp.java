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

import java.util.List;

import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.SqsDataTooLargeException;
import io.datarouter.aws.sqs.op.SqsBlobOp;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.util.concurrent.UncheckedInterruptedException;
import software.amazon.awssdk.core.exception.AbortedException;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class SqsBlobPutOp extends SqsBlobOp<Void>{

	private final byte[] data;
	private final int maxRawDataSize;

	public SqsBlobPutOp(
			byte[] data,
			int maxRawDataSize,
			Config config,
			SqsClientManager sqsClientManager,
			ClientId clientId,
			String queueUrl){
		super(sqsClientManager, clientId, config, queueUrl);
		this.data = data;
		this.maxRawDataSize = maxRawDataSize;
	}

	@Override
	protected Void run(){
		if(data.length > maxRawDataSize){
			throw new SqsDataTooLargeException(List.of("a blob of size " + data.length));
		}
		var request = SendMessageRequest.builder()
				.queueUrl(queueUrl)
				.messageBody(SqsBlobOp.SQS_BLOB_BASE_64_CODEC.encode(data))
				.build();
		try{
			sqsClientManager.getAmazonSqs(clientId).sendMessage(request);
		}catch(AbortedException e){
			throw new UncheckedInterruptedException("", e);
		}
		return null;
	}

}
