package com.hotpads.conveyor.queue;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.conveyor.Conveyor;
import com.hotpads.conveyor.ConveyorCounters;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.GroupQueueStorage;
import com.hotpads.datarouter.node.op.raw.write.StorageWriter;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.GroupQueueMessage;

public class GroupQueueConveyor<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
implements Runnable, Conveyor{
	private static final Logger logger = LoggerFactory.getLogger(GroupQueueConveyor.class);

	private static final Duration PEEK_TIMEOUT = Duration.ofSeconds(5);
	private static final Config PEEK_CONFIG = new Config().setTimeoutMs(PEEK_TIMEOUT.toMillis());

	private final String name;
	private final Setting<Boolean> shouldRunSetting;
	private final GroupQueueStorage<PK,D> groupQueueStorage;
	private final StorageWriter<PK,D> storageWriter;


	public GroupQueueConveyor(String name, Setting<Boolean> shouldRunSetting, GroupQueueStorage<PK,D> groupQueueStorage,
			StorageWriter<PK,D> storageWriter){
		this.name = name;
		this.shouldRunSetting = shouldRunSetting;
		this.groupQueueStorage = groupQueueStorage;
		this.storageWriter = storageWriter;
	}


	@Override
	public void run(){
		try{
			for(GroupQueueMessage<PK,D> message : groupQueueStorage.peekUntilEmpty(PEEK_CONFIG)){
				List<D> databeans = message.getDatabeans();
				storageWriter.putMulti(databeans, null);
				ConveyorCounters.inc(this, "putMulti ops", 1);
				ConveyorCounters.inc(this, "putMulti databeans", databeans.size());
				groupQueueStorage.ack(message.getKey(), null);
				ConveyorCounters.inc(this, "ack", 1);
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
