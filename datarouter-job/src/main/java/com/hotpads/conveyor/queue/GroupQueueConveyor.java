package com.hotpads.conveyor.queue;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.conveyor.BaseConveyor;
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
extends BaseConveyor<PK,D>{
	private static final Logger logger = LoggerFactory.getLogger(GroupQueueConveyor.class);

	private static final Duration PEEK_TIMEOUT = Duration.ofSeconds(5);
	private static final Config PEEK_CONFIG = new Config().setTimeoutMs(PEEK_TIMEOUT.toMillis());

	private final GroupQueueStorage<PK,D> groupQueueStorage;
	private final StorageWriter<PK,D> storageWriter;


	public GroupQueueConveyor(String name, Setting<Boolean> shouldRunSetting, GroupQueueStorage<PK,D> groupQueueStorage,
			StorageWriter<PK,D> storageWriter){
		super(name, shouldRunSetting);
		this.groupQueueStorage = groupQueueStorage;
		this.storageWriter = storageWriter;
	}


	@Override
	public ProcessBatchResult processBatch(){
		GroupQueueMessage<PK,D> message = groupQueueStorage.peek(PEEK_CONFIG);
		if(message == null){
			logger.info("peeked conveyor={} nullMessage", name);
			return new ProcessBatchResult(false);
		}
		List<D> databeans = message.getDatabeans();
		logger.info("peeked conveyor={} messageCount={}", name, databeans.size());
		storageWriter.putMulti(databeans, null);
		logger.info("wrote conveyor={} messageCount={}", name, databeans.size());
		ConveyorCounters.incPutMultiOpAndDatabeans(this, databeans.size());
		groupQueueStorage.ack(message.getKey(), null);
		logger.info("acked conveyor={} messageCount={}", name, databeans.size());
		ConveyorCounters.incAck(this);
		return new ProcessBatchResult(true);
	}

}
