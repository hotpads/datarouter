 package com.hotpads.datarouter.node;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;


public interface Node<PK extends PrimaryKey<PK>,D extends Databean<PK>> {

	String getName();
	Class<D> getDatabeanType();
	
	List<String> getClientNames();
	boolean usesClient(String clientName);
	<K extends UniqueKey<PK>> List<String> getClientNamesForKeys(Collection<K> keys);
	List<? extends PhysicalNode<PK,D>> getPhysicalNodes();
	List<? extends PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName);
	Node<PK,D> getMaster();
	
	void clearThreadSpecificState();


	Map<PK,D> getByKey(Iterable<D> databaeans);
}
