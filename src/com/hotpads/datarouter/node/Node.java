 package com.hotpads.datarouter.node;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


public interface Node<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends Comparable<Node<PK,D>>{

	String getName();
	Class<PK> getPrimaryKeyType();
	Class<D> getDatabeanType();
	List<Field<?>> getFields();
	
	List<Field<?>> getFields(D d);
	
	Set<String> getAllNames();
	List<String> getClientNames();
	boolean usesClient(String clientName);
	List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys);
	List<? extends PhysicalNode<PK,D>> getPhysicalNodes();
	List<? extends PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName);
	Node<PK,D> getMaster();
	List<? extends Node<PK,D>> getChildNodes();
	
	void clearThreadSpecificState();
	
}
