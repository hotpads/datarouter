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
package io.datarouter.conveyor.queue;

import java.util.List;

import io.datarouter.conveyor.BaseConveyor;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.DatabeanBuffer;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.node.op.raw.write.StorageWriter;
import io.datarouter.storage.setting.Setting;

public class DatabeanBufferConveyor<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> extends BaseConveyor{

	private static final int BATCH_SIZE = 100;

	private final DatabeanBuffer<PK,D> databeanBuffer;
	private final StorageWriter<PK,D> storageWriter;

	public DatabeanBufferConveyor(
			String name,
			Setting<Boolean> shouldRun,
			DatabeanBuffer<PK,D> databeanBuffer,
			StorageWriter<PK,D> storageWriter){
		super(name, shouldRun, () -> false);
		this.databeanBuffer = databeanBuffer;
		this.storageWriter = storageWriter;
	}

	@Override
	public ProcessBatchResult processBatch(){
		List<D> databeans = databeanBuffer.pollMultiWithLimit(BATCH_SIZE);
		if(databeans.isEmpty()){
			return new ProcessBatchResult(false);
		}
		try{
			storageWriter.putMulti(databeans);
			ConveyorCounters.incPutMultiOpAndDatabeans(this, databeans.size());
			return new ProcessBatchResult(true);
		}catch(Exception putMultiException){
			databeans.forEach(databeanBuffer::offer);// might as well try to save them for later
			ConveyorCounters.inc(this, "putMulti exception", 1);
			return new ProcessBatchResult(false);// backoff for a bit
		}
	}

}
