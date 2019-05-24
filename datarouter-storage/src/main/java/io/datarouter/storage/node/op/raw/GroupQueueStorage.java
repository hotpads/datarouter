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
package io.datarouter.storage.node.op.raw;

import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.GroupQueueStorageReader;
import io.datarouter.storage.node.op.raw.write.QueueStorageWriter;

public interface GroupQueueStorage<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends QueueStorageWriter<PK,D>, GroupQueueStorageReader<PK,D>{

	public static final String OP_pollMulti = "pollMulti";
	public static final String OP_pollUntilEmpty = "pollUntilEmpty";

	List<D> pollMulti(Config config);

	public interface GroupQueueStorageNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends GroupQueueStorage<PK,D>, QueueStorageWriterNode<PK,D,F>{
	}

	public interface PhysicalGroupQueueStorageNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends GroupQueueStorageNode<PK,D,F>, PhysicalQueueStorageWriterNode<PK,D,F>{
	}

}
