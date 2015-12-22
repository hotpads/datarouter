package com.hotpads.datarouter.node.op.raw;

import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.GroupQueueStorageReader;
import com.hotpads.datarouter.node.op.raw.write.QueueStorageWriter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface GroupQueueStorage<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends QueueStorageWriter<PK,D>, GroupQueueStorageReader<PK,D>{

	public static final String
			OP_pollMulti = "pollMulti",
			OP_pollUntilEmpty = "pollUntilEmpty"
			;
	
	List<D> pollMulti(Config config);
	
	public interface PhysicalGroupQueueStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends GroupQueueStorage<PK,D>,PhysicalQueueStorageWriterNode<PK,D>{
		
	}
}
