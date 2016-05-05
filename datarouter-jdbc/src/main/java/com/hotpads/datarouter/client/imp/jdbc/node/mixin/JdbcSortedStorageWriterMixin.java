package com.hotpads.datarouter.client.imp.jdbc.node.mixin;

import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter.PhysicalSortedStorageWriterNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface JdbcSortedStorageWriterMixin<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends PhysicalSortedStorageWriterNode<PK,D>, JdbcStorageMixin{

}
