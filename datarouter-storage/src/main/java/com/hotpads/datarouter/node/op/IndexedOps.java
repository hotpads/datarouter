package com.hotpads.datarouter.node.op;

import java.util.List;

import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;

public interface IndexedOps<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{
	<IK extends PrimaryKey<IK>, 
	IE extends IndexEntry<IK,IE,PK,D>, 
	IF extends DatabeanFielder<IK,IE>, 
	N extends ManagedNode<PK,D,IK,IE,IF>> N registerManaged(N managedNode);

	List<ManagedNode<PK,D,?,?,?>> getManagedNodes();

}
