 package com.hotpads.datarouter.node;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


public interface Node<D extends Databean<PK>,PK extends PrimaryKey<PK>> {

	String getName();
	Class<D> getDatabeanType();
	
	List<String> getClientNames();
	boolean usesClient(String clientName);
	<K extends UniqueKey<PK>> List<String> getClientNamesForKeys(Collection<K> keys);
	List<? extends PhysicalNode<D,PK>> getPhysicalNodes();
	List<? extends PhysicalNode<D,PK>> getPhysicalNodesForClient(String clientName);
	Node<D,PK> getMaster();
	
	void clearThreadSpecificState();
	
}
