package com.hotpads.datarouter.client.imp.sqs;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.QueueStorage.PhysicalQueueStorageNode;
import com.hotpads.datarouter.op.scan.queue.PeekUntilEmptyQueueStorageScanner;
import com.hotpads.datarouter.op.scan.queue.PollUntilEmptyQueueStorageIterable;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.BaseQueueMessage;
import com.hotpads.datarouter.storage.queue.QueueMessage;
import com.hotpads.datarouter.storage.queue.QueueMessageKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.iterable.scanner.iterable.ScannerIterable;

public class SqsNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseSqsNode<PK,D,F>
implements PhysicalQueueStorageNode<PK,D>{
	
	public SqsNode(DatarouterContext datarouterContext, NodeParams<PK,D,F> params){
		super(datarouterContext, params);
	}
	
	// Reader
	
	@Override
	public QueueMessage<PK,D> peek(Config config){
		config = Config.nullSafe(config).setLimit(1);
		return DrCollectionTool.getFirst(sqsOpFactory.makePeekMultiOp(config).call());
	}
	
	@Override
	public List<QueueMessage<PK, D>> peekMulti(Config config){
		return sqsOpFactory.makePeekMultiOp(config).call();
	}

	@Override
	public Iterable<QueueMessage<PK, D>> peekUntilEmpty(Config config){
		return new ScannerIterable<>(new PeekUntilEmptyQueueStorageScanner<>(this, config));
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
	
	@Override
	public void ack(QueueMessageKey key, Config config){
		sqsOpFactory.makeAckOp(key, config);
	}

	@Override
	public void ackMulti(Collection<QueueMessageKey> keys, Config config){
		sqsOpFactory.makeAckMultiOp(keys, config);
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

	@Override
	public List<D> pollMulti(Config config){
		List<QueueMessage<PK, D>> messages = peekMulti(config);
		ackMulti(BaseQueueMessage.getKeys(messages), config);
		return QueueMessage.getDatabeans(messages);
	}

	@Override
	public Iterable<D> pollUntilEmpty(Config config){
		return new PollUntilEmptyQueueStorageIterable<>(this, config);
	}
}