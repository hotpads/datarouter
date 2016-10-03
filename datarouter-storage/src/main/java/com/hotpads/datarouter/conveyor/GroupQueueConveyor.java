package com.hotpads.datarouter.conveyor;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.hotpads.datarouter.node.op.raw.GroupQueueStorage;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.GroupQueueMessage;

public class GroupQueueConveyor<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
implements Runnable{

	private final Setting<Boolean> shouldRunSetting;
	private final GroupQueueStorage<PK,D> groupQueueStorage;
	private final MapStorage<PK,D> mapStorage;

	private final ScheduledExecutorService exec;

	private long numDatabeansDrained = 0;


	public GroupQueueConveyor(Setting<Boolean> shouldRunSetting, GroupQueueStorage<PK,D> groupQueueStorage,
			MapStorage<PK,D> mapStorage){
		this.shouldRunSetting = shouldRunSetting;
		this.groupQueueStorage = groupQueueStorage;
		this.mapStorage = mapStorage;

		this.exec = Executors.newSingleThreadScheduledExecutor();
		exec.scheduleWithFixedDelay(this, 0, 5, TimeUnit.SECONDS);
	}


	@Override
	public void run(){
		for(GroupQueueMessage<PK,D> message : groupQueueStorage.peekUntilEmpty(null)){
			List<D> databeans = message.getDatabeans();
			mapStorage.putMulti(databeans, null);
			numDatabeansDrained += databeans.size();
			groupQueueStorage.ack(message.getKey(), null);
			if(!shouldRunSetting.getValue()){
				break;
			}
		}
	}

}
