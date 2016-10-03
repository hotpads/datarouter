package com.hotpads.conveyor.queue;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.conveyor.Conveyor;
import com.hotpads.conveyor.ConveyorCounters;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.GroupQueueStorage;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.GroupQueueMessage;
import com.hotpads.util.core.concurrent.ThreadTool;

public class GroupQueueConveyor<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
implements Conveyor, Runnable{
	private static final Logger logger = LoggerFactory.getLogger(GroupQueueConveyor.class);

	private static final Duration PEEK_TIMEOUT = Duration.ofSeconds(5);//will delay server shutdown
	private static final Config PEEK_CONFIG = new Config().setTimeoutMs(PEEK_TIMEOUT.toMillis());
	private static final Duration SLEEP_DURATION = Duration.ofSeconds(5);

	private final String name;
	private final Setting<Boolean> shouldRunSetting;
	private final GroupQueueStorage<PK,D> groupQueueStorage;
	private final MapStorage<PK,D> mapStorage;

	private final AtomicBoolean shutdownRequested;


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
		while(!shutdownRequested.get()){
			try{
				if(shouldRun()){
					for(GroupQueueMessage<PK,D> message : groupQueueStorage.peekUntilEmpty(PEEK_CONFIG)){
						List<D> databeans = message.getDatabeans();
						mapStorage.putMulti(databeans, null);
						ConveyorCounters.inc(this, "putMulti ops", 1);
						ConveyorCounters.inc(this, "putMulti databeans", databeans.size());
						groupQueueStorage.ack(message.getKey(), null);
						ConveyorCounters.inc(this, "ack", 1);
						if(!shouldRun()){
							break;
						}
					}
				}else{
					ThreadTool.sleep(SLEEP_DURATION.toMillis());
				}
			}catch(Exception e){
				logger.error("", e);
				ThreadTool.sleep(SLEEP_DURATION.toMillis());
			}
		}
		logger.warn("exiting {}", getName());
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
