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
package io.datarouter.aws.sqs.op;

import java.util.concurrent.Callable;

import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.bytes.codec.bytestringcodec.Base64ByteStringCodec;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;

public abstract class SqsBlobOp<V> implements Callable<V>{

	public static final Base64ByteStringCodec SQS_BLOB_BASE_64_CODEC = Base64ByteStringCodec.INSTANCE;

	protected final SqsClientManager sqsClientManager;
	protected final ClientId clientId;
	protected final Config config;
	protected final String queueUrl;

	public SqsBlobOp(SqsClientManager sqsClientManager, ClientId clientId, Config config, String queueUrl){
		this.sqsClientManager = sqsClientManager;
		this.clientId = clientId;
		this.config = config;
		this.queueUrl = queueUrl;
	}

	@Override
	public V call(){
		return run();
	}

	protected abstract V run();

}
