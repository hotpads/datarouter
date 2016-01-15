package com.hotpads.datarouter.node.entity;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.SortedMapStorageReaderNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;

public interface EntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>{

	Datarouter getContext();
	String getName();
	
	<PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>>
	void register(SortedMapStorageReaderNode<PK,D> subEntityNode);

	Collection<Node<?,?>> getSubEntityNodes();
	
	E getEntity(EK key, Config config);
	
}
