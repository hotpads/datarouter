package com.hotpads.conveyor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class BaseConveyor<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
implements Conveyor{
	private static final Logger logger = LoggerFactory.getLogger(BaseConveyor.class);


	private final String name;
	private final Setting<Boolean> shouldRunSetting;


	public BaseConveyor(String name, Setting<Boolean> shouldRunSetting){
		this.name = name;
		this.shouldRunSetting = shouldRunSetting;
	}


	public abstract ProcessBatchResult processBatch();


	protected static class ProcessBatchResult{
		public final boolean shouldContinueImmediately;

		public ProcessBatchResult(boolean shouldContinueImmediately){
			this.shouldContinueImmediately = shouldContinueImmediately;
		}
	}


	@Override
	public void run(){
		try{
			while(shouldRun()){
				ProcessBatchResult result = processBatch();
				if(!result.shouldContinueImmediately){
					return;
				}
			}
		}catch(Exception e){
			ConveyorCounters.incException(this);
			logger.warn("swallowing exception so ScheduledExecutorService restarts this Runnable", e);
		}
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public boolean shouldRun(){
		return !Thread.currentThread().isInterrupted() && shouldRunSetting.getValue();
	}
}
