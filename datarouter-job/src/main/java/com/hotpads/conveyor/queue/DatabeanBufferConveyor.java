package com.hotpads.conveyor.queue;

import java.util.List;

import com.hotpads.conveyor.BaseConveyor;
import com.hotpads.conveyor.ConveyorCounters;
import com.hotpads.conveyor.DatabeanBuffer;
import com.hotpads.datarouter.node.op.raw.write.StorageWriter;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class DatabeanBufferConveyor<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseConveyor<PK,D>{

	private static final int BATCH_SIZE = 100;

	private final DatabeanBuffer<PK,D> databeanBuffer;
	private final StorageWriter<PK,D> storageWriter;


	public DatabeanBufferConveyor(String name, Setting<Boolean> shouldRunSetting, DatabeanBuffer<PK,D> databeanBuffer,
			StorageWriter<PK,D> storageWriter){
		super(name, shouldRunSetting);
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
			storageWriter.putMulti(databeans, null);
			ConveyorCounters.inc(this, "putMulti ops", 1);
			ConveyorCounters.inc(this, "putMulti databeans", databeans.size());
			return new ProcessBatchResult(true);
		}catch(Exception putMultiException){
			databeans.forEach(databeanBuffer::offer);//might as well try to save them for later
			ConveyorCounters.inc(this, "putMulti exception", 1);
			return new ProcessBatchResult(false);//backoff for a bit
		}
	}

}
