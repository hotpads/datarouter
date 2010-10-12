package com.hotpads.datarouter.node.op.raw;

import com.hotpads.datarouter.node.op.raw.read.TableStorageReader;
import com.hotpads.datarouter.node.op.raw.write.TableStorageWriter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface TableStorage<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends TableStorageReader<PK,D>, TableStorageWriter<PK,D>
{
	public interface TableStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends TableStorageReaderNode<PK,D>, TableStorageWriterNode<PK,D>
	{
	}
	
	public interface PhysicalTableStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
	extends PhysicalTableStorageReaderNode<PK,D>, PhysicalTableStorageWriterNode<PK,D>
	{
	}
}
