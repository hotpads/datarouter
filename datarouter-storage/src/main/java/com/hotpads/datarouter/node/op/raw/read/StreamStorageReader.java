package com.hotpads.datarouter.node.op.raw.read;

import java.util.concurrent.BlockingQueue;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.DatarouterStreamSubscriberConfig;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.stream.StreamRecord;

/**
 * Methods for reading from a stream where each record contains a single Databean.
 */
public interface StreamStorageReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends NodeOps<PK,D>{

	public static final String OP_subscribe = "subscribe";

	BlockingQueue<StreamRecord<PK,D>> subscribe(DatarouterStreamSubscriberConfig streamConfig, Config config);
}
