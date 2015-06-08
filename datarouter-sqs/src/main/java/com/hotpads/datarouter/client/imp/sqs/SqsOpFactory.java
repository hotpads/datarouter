package com.hotpads.datarouter.client.imp.sqs;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.imp.sqs.op.SqsAckMultiOp;
import com.hotpads.datarouter.client.imp.sqs.op.SqsAckOp;
import com.hotpads.datarouter.client.imp.sqs.op.SqsOp;
import com.hotpads.datarouter.client.imp.sqs.op.SqsPeekMultiOp;
import com.hotpads.datarouter.client.imp.sqs.op.SqsPutMultiOp;
import com.hotpads.datarouter.client.imp.sqs.op.SqsPutOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.QueueMessage;
import com.hotpads.datarouter.storage.queue.QueueMessageKey;

public class SqsOpFactory<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>{
	
	private final SqsNode<PK,D,F> sqsNode;
	
	public SqsOpFactory(SqsNode<PK,D,F> sqsNode){
		this.sqsNode = sqsNode;
	}

	public SqsOp<PK,D,F,List<QueueMessage<PK,D>>> makePeekMultiOp(Config config){
		return new SqsPeekMultiOp<>(config, sqsNode);
	}
	
	public SqsOp<PK,D,F,Void> makeAckMultiOp(Collection<QueueMessageKey> keys, Config config){
		return new SqsAckMultiOp<>(keys, config, sqsNode);
	}
	
	public SqsOp<PK,D,F,Void> makePutMultiOp(Collection<D> databeans, Config config){
		return new SqsPutMultiOp<>(databeans, config, sqsNode);
	}
	
	public SqsOp<PK,D,F,Void> makePutOp(D databean, Config config){
		return new SqsPutOp<>(databean, config, sqsNode);
	}
	
	public SqsOp<PK,D,F,Void> makeAckOp(QueueMessageKey key, Config config){
		return new SqsAckOp<>(key, config, sqsNode);
	}
}
