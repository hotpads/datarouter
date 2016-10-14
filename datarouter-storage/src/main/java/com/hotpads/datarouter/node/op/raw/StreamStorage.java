package com.hotpads.datarouter.node.op.raw;

import com.hotpads.datarouter.node.op.raw.read.StreamStorageReader;
import com.hotpads.datarouter.node.op.raw.write.StorageWriter;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface StreamStorage<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends StorageWriter<PK,D>, StreamStorageReader<PK,D>{

	/*************** sub-interfaces ***********************/

	public interface PhysicalStreamStorageNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	extends StreamStorage<PK,D>, PhysicalNode<PK,D>{
	}
}
