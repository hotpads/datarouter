package com.hotpads.datarouter.node.op.raw;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface MapStorage<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends MapStorageReader<PK,D>, MapStorageWriter<PK,D>
{
	public interface MapStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends Node<PK,D>, MapStorage<PK,D>, 
			MapStorageReaderNode<PK,D>, MapStorageWriterNode<PK,D>
	{
	}
	
	public interface PhysicalMapStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends PhysicalNode<PK,D>, MapStorageNode<PK,D>, 
			PhysicalMapStorageReaderNode<PK,D>, PhysicalMapStorageWriterNode<PK,D>
	{
	}
}
