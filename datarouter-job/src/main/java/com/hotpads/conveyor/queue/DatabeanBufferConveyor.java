package com.hotpads.conveyor.queue;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.conveyor.Conveyor;
import com.hotpads.conveyor.ConveyorCounters;
import com.hotpads.conveyor.DatabeanBuffer;
import com.hotpads.datarouter.node.op.raw.write.StorageWriter;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class DatabeanBufferConveyor<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
implements Runnable, Conveyor{
	private static final Logger logger = LoggerFactory.getLogger(DatabeanBufferConveyor.class);

	private static final int BATCH_SIZE = 100;

	private final String name;
	private final Setting<Boolean> shouldRunSetting;
	private final DatabeanBuffer<PK,D> databeanBuffer;
	private final StorageWriter<PK,D> storageWriter;


	public DatabeanBufferConveyor(String name, Setting<Boolean> shouldRunSetting, DatabeanBuffer<PK,D> databeanBuffer,
			StorageWriter<PK,D> storageWriter){
		this.name = name;
		this.shouldRunSetting = shouldRunSetting;
		this.databeanBuffer = databeanBuffer;
		this.storageWriter = storageWriter;
	}


	@Override
	public void run(){
		try{
			while(true){
				List<D> databeans = databeanBuffer.poll(BATCH_SIZE);
				if(databeans.isEmpty()){
					return;
				}
				try{
					storageWriter.putMulti(databeans, null);
					ConveyorCounters.inc(this, "putMulti ops", 1);
					ConveyorCounters.inc(this, "putMulti databeans", databeans.size());
				}catch(Exception putMultiException){
					databeans.forEach(databeanBuffer::offer);//might as well try to save them for later
					ConveyorCounters.inc(this, "putMulti exception", 1);
				}
				if(!shouldRun()){
					break;
				}
			}
		}catch(Exception e){
			logger.warn("swallowing exception so ScheduledExecutorService restarts this Runnable", e);
		}
	}

	@Override
	public String getName(){
		return name;
	}

	private boolean shouldRun(){
		return !Thread.currentThread().isInterrupted() && shouldRunSetting.getValue();
	}

}
