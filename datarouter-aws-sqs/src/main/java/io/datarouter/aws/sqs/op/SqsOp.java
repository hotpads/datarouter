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
package io.datarouter.aws.sqs.op;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.StringDatabeanCodec;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public abstract class SqsOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		V>
implements Callable<V>{

	protected final Config config;
	protected final String queueUrl;
	protected final Supplier<D> databeanSupplier;
	protected final F fielder;
	protected final StringDatabeanCodec codec;
	protected final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;

	public SqsOp(Config config, BaseSqsNode<PK,D,F> sqsNode){
		this.config = config;
		this.queueUrl = sqsNode.getQueueUrl().get();
		this.databeanSupplier = sqsNode.getFieldInfo().getDatabeanSupplier();
		this.fielder = sqsNode.getFieldInfo().getSampleFielder();
		this.codec = fielder.getStringDatabeanCodec();
		this.fieldInfo = sqsNode.getFieldInfo();
	}

	@Override
	public V call(){
		// count
		return run();
	}

	protected abstract V run();

}
