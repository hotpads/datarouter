package com.hotpads.datarouter.node;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;


public interface Node<D extends Databean,PK extends PrimaryKey<D>> {

	String getName();
	Class<D> getDatabeanType();
	
	List<String> getClientNames();
	boolean usesClient(String clientName);
	<K extends UniqueKey<D>> List<String> getClientNamesForKeys(Collection<K> keys);
	List<? extends PhysicalNode<D,PK>> getPhysicalNodes();
	List<? extends PhysicalNode<D,PK>> getPhysicalNodesForClient(String clientName);
	Node<D,PK> getMaster();
	
	void clearThreadSpecificState();
	
}
