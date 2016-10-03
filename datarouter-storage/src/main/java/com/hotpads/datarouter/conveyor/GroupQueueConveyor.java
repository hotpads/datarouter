package com.hotpads.datarouter.conveyor;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.node.op.raw.GroupQueueStorage;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.GroupQueueMessage;

public class GroupQueueConveyor<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
implements Conveyor, Runnable{
	private static final Logger logger = LoggerFactory.getLogger(GroupQueueConveyor.class);

	private static final Duration SLEEP_DURATION = Duration.ofSeconds(5);

	private final String name;
	private final Setting<Boolean> shouldRunSetting;
	private final GroupQueueStorage<PK,D> groupQueueStorage;
	private final MapStorage<PK,D> mapStorage;

	private final AtomicBoolean shutdownRequested;
	private long numDatabeansDrained = 0;


	public GroupQueueConveyor(String name, Setting<Boolean> shouldRunSetting, GroupQueueStorage<PK,D> groupQueueStorage,
			MapStorage<PK,D> mapStorage){
		this.name = name;
		this.shouldRunSetting = shouldRunSetting;
		this.groupQueueStorage = groupQueueStorage;
		this.mapStorage = mapStorage;
		this.shutdownRequested = new AtomicBoolean(false);
	}


	@Override
	public void run(){
		while(true){
			try{
				if(shouldRun()){
					for(GroupQueueMessage<PK,D> message : groupQueueStorage.peekUntilEmpty(null)){
						List<D> databeans = message.getDatabeans();
						mapStorage.putMulti(databeans, null);
						numDatabeansDrained += databeans.size();
						groupQueueStorage.ack(message.getKey(), null);
						if(!shouldRun()){
							break;
						}
					}
				}
			}catch(Exception e){
				logger.error("", e);
			}finally{
				try{
					Thread.sleep(SLEEP_DURATION.toMillis());
				}catch(InterruptedException e){
					Thread.currentThread().isInterrupted();//clear flag
				}
			}
		}
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public void shutdown(){
		shutdownRequested.set(true);
	}

	private boolean shouldRun(){
		return shouldRunSetting.getValue() && !shutdownRequested.get();
	}

}
