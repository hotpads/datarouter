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

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.op.raw.read.ObjectStorageReader;
import io.datarouter.storage.node.op.raw.write.ObjectStorageWriter;
import io.datarouter.storage.node.type.physical.PhysicalNode;

public interface ObjectStorage<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends ObjectStorageReader<PK,D>, ObjectStorageWriter<PK,D>{

	/*---------------------------- sub-interfaces ---------------------------*/

	public interface ObjectStorageNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends ObjectStorage<PK,D>{
	}

	public interface PhysicalObjectStorageNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends ObjectStorageNode<PK,D,F>, PhysicalNode<PK,D,F>{
	}

}
