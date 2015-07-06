package com.hotpads.datarouter.client.imp.sqs;

import java.util.Collection;

import com.hotpads.datarouter.client.imp.sqs.group.SqsGroupNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.QueueStorage.PhysicalQueueStorageNode;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.QueueMessage;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class SqsNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SqsGroupNode<PK,D,F>
implements PhysicalQueueStorageNode<PK,D>{
	
	//do not change, this is a limit from SQS
	public static final int MAX_MESSAGES_PER_BATCH = 10;
	public static final int MAX_TIMEOUT_SECONDS = 20;
	public static final int MAX_BYTES_PER_MESSAGE = 256*1024;
	public static final int MAX_BYTES_PER_PAYLOAD = 256*1024;
	
	public SqsNode(DatarouterContext datarouterContext, NodeParams<PK,D,F> params){
		super(datarouterContext, params);
	}
	
	// Reader
	
	@Override
	public QueueMessage<PK,D> peek(Config config){
		config = Config.nullSafe(config).setLimit(1);
		return DrCollectionTool.getFirst(sqsOpFactory.makePeekMultiOp(config).call());
	}
	
	// Writer
	
	@Override
	public void put(D databean, Config config){
		sqsOpFactory.makePutOp(databean, config).call();
	}
	
	@Override
	public void putMulti(Collection<D> databeans, Config config){
		sqsOpFactory.makePutMultiOp(databeans, config).call();
	}
	
	// Reader + Writer
	
	@Override
	public D poll(Config config){
		QueueMessage<PK,D> message = peek(config);
		if(message == null){
			return null;
		}
		ack(message.getKey(), config);
		return message.getDatabean();
	}
}