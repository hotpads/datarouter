package com.hotpads.datarouter.client.imp.sqs.group;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.client.imp.sqs.BaseSqsNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.GroupQueueStorage.PhysicalGroupQueueStorageNode;
import com.hotpads.datarouter.op.scan.queue.group.PeekGroupUntilEmptyQueueStorageScanner;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.BaseQueueMessage;
import com.hotpads.datarouter.storage.queue.GroupQueueMessage;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.iterable.scanner.iterable.ScannerIterable;

public class SqsGroupNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseSqsNode<PK,D,F> 
implements PhysicalGroupQueueStorageNode<PK,D>{
	
	public SqsGroupNode(Datarouter datarouter, NodeParams<PK, D, F> params){
		super(datarouter, params);
	}

	//Writer

	@Override
	public void put(D databean, Config config){
		sqsOpFactory.makeGroupPutMultiOp(Collections.singleton(databean), config).call();
	}
	
	@Override
	public void putMulti(Collection<D> databeans, Config config){
		sqsOpFactory.makeGroupPutMultiOp(databeans, config).call();
	}
	
	//Reader
	
	@Override
	public GroupQueueMessage<PK, D> peek(Config config){
		Config nullSafeConfig = Config.nullSafe(config).setLimit(1);
		return DrCollectionTool.getFirst(peekMulti(nullSafeConfig));
	}
	
	@Override
	public List<GroupQueueMessage<PK,D>> peekMulti(Config config){
		return sqsOpFactory.makeGroupPeekMultiOp(config).call();
	}
	
	@Override
	public Iterable<GroupQueueMessage<PK,D>> peekUntilEmpty(Config config){
		return new ScannerIterable<>(new PeekGroupUntilEmptyQueueStorageScanner<>(this, config));
	}
	
	//Reader + Writer
	
	@Override
	public List<D> pollMulti(Config config){
		List<GroupQueueMessage<PK,D>> results = peekMulti(config);
		ackMulti(BaseQueueMessage.getKeys(results), config);
		return GroupQueueMessage.getDatabeans(results);
	}
}
