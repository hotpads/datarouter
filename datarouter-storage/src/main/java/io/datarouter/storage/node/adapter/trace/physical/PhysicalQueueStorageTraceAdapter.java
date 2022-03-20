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
package io.datarouter.storage.node.adapter.trace.physical;

import java.util.List;

import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.instrumentation.trace.TracerTool.TraceSpanInfoBuilder;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.PhysicalAdapterMixin;
import io.datarouter.storage.node.adapter.trace.QueueStorageWriterTraceAdapter;
import io.datarouter.storage.node.op.raw.QueueStorage.PhysicalQueueStorageNode;
import io.datarouter.storage.queue.QueueMessage;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class PhysicalQueueStorageTraceAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalQueueStorageNode<PK,D,F>>
extends QueueStorageWriterTraceAdapter<PK,D,F,N>
implements PhysicalQueueStorageNode<PK,D,F>, PhysicalAdapterMixin<PK,D,F,N>{

	public PhysicalQueueStorageTraceAdapter(N backingNode){
		super(backingNode);
	}

	@Override
	public QueueMessage<PK,D> peek(Config config){
		try(var $ = startSpanForOp(OP_peek)){
			QueueMessage<PK,D> databean = backingNode.peek(config);
			TracerTool.appendToSpanInfo(databean != null ? "hit" : "miss");
			return databean;
		}
	}

	@Override
	public List<QueueMessage<PK,D>> peekMulti(Config config){
		try(var $ = startSpanForOp(OP_peekMulti)){
			List<QueueMessage<PK,D>> messages = backingNode.peekMulti(config);
			TracerTool.appendToSpanInfo(new TraceSpanInfoBuilder().databeans(messages.size()));
			return messages;
		}
	}

	@Override
	public Scanner<QueueMessage<PK,D>> peekUntilEmpty(Config config){
		return backingNode.peekUntilEmpty(config);
	}

	@Override
	public D poll(Config config){
		try(var $ = startSpanForOp(OP_poll)){
			D databean = backingNode.poll(config);
			TracerTool.appendToSpanInfo(databean != null ? "hit" : "miss");
			return databean;
		}
	}

	@Override
	public List<D> pollMulti(Config config){
		try(var $ = startSpanForOp(OP_pollMulti)){
			List<D> databeans = backingNode.pollMulti(config);
			TracerTool.appendToSpanInfo(new TraceSpanInfoBuilder().databeans(databeans.size()));
			return databeans;
		}
	}

	@Override
	public Scanner<D> pollUntilEmpty(Config config){
		return backingNode.pollUntilEmpty(config);
	}

	@Override
	public PhysicalDatabeanFieldInfo<PK,D,F> getFieldInfo(){
		return PhysicalAdapterMixin.super.getFieldInfo();
	}

}
