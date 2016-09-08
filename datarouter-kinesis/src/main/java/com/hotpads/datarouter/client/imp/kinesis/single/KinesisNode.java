package com.hotpads.datarouter.client.imp.kinesis.single;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;

import com.hotpads.datarouter.client.imp.kinesis.BaseKinesisNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.DatarouterStreamSubscriberConfig;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.StreamStorage;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.stream.StreamRecord;

public class KinesisNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseKinesisNode<PK,D,F>
implements StreamStorage<PK,D>{

	public KinesisNode(Datarouter datarouter, NodeParams<PK,D,F> params){
		super(datarouter, params);
	}

	// Reader

	@Override
	public BlockingQueue<StreamRecord<PK,D>> subscribe(DatarouterStreamSubscriberConfig streamSubscriberConfig, Config config){
		return kinesisOpFactory.makeStreamSubscriberOp(streamSubscriberConfig, config).call();
	}

	// Writer

	@Override
	public void put(D databean, Config config){
		kinesisOpFactory.makePutOp(databean, config).call();
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		kinesisOpFactory.makePutMultiOp(databeans, config).call();
	}

}