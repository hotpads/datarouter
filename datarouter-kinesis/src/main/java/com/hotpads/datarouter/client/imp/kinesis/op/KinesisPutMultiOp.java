package com.hotpads.datarouter.client.imp.kinesis.op;

import java.util.Collection;

import com.hotpads.datarouter.client.imp.kinesis.node.BaseKinesisNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.exception.NotImplementedException;

public class KinesisPutMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends KinesisOp<PK,D,F,Void>{

	private final Collection<D> databeans;

	public KinesisPutMultiOp(Collection<D> databeans, Config config, BaseKinesisNode<PK,D,F> kinesisNode){
		super(config, kinesisNode);
		this.databeans = databeans;
	}

	@Override
	protected Void run(){
		//TODO DATAROUTER-401
		throw new NotImplementedException();
	}

}
