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
package io.datarouter.storage.node.adapter.trace;

import java.util.Collection;

import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.write.QueueStorageWriter;
import io.datarouter.storage.node.op.raw.write.QueueStorageWriter.PhysicalQueueStorageWriterNode;
import io.datarouter.storage.queue.QueueMessageKey;

public class QueueStorageWriterTraceAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalQueueStorageWriterNode<PK,D,F>>
extends BaseTraceAdapter<PK,D,F,N>
implements QueueStorageWriter<PK,D>{

	public QueueStorageWriterTraceAdapter(N backingNode){
		super(backingNode);
	}

	@Override
	public void put(D databean, Config config){
		try(var _ = startSpanForOp(OP_put)){
			backingNode.put(databean, config);
		}
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		try(var _ = startSpanForOp(OP_putMulti)){
			backingNode.putMulti(databeans, config);
			TracerTool.appendToSpanInfo("databeans", databeans.size());
		}
	}

	@Override
	public void ack(QueueMessageKey key, Config config){
		try(var _ = startSpanForOp(OP_ack)){
			backingNode.ack(key, config);
		}
	}

	@Override
	public void ackMulti(Collection<QueueMessageKey> keys, Config config){
		try(var _ = startSpanForOp(OP_ackMulti)){
			backingNode.ackMulti(keys, config);
			TracerTool.appendToSpanInfo("keys", keys.size());
		}
	}

}
