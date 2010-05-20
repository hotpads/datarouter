package com.hotpads.datarouter.node;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


public interface Node<D extends Databean> {

	String getName();
	Class<D> getDatabeanType();
	
	List<String> getClientNames();
	boolean usesClient(String clientName);
	<K extends UniqueKey<D>> List<String> getClientNamesForKeys(Collection<K> keys);
	List<? extends PhysicalNode<D>> getPhysicalNodes();
	List<? extends PhysicalNode<D>> getPhysicalNodesForClient(String clientName);
	Node<D> getMaster();
	
	void clearThreadSpecificState();
	
}
