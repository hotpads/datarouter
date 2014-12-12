 package com.hotpads.datarouter.node;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

/**
 * A Node is the interface through which the application sends Databeans for serialization and storage. It ties together
 * a PrimaryKey type, a Databean type, a Fielder type. A Node can be a PhysicalNode or a virtual node, like
 * MasterSlaveNode, that forwards requests on to other nodes.
 * 
 * @author mcorgan
 * 
 * @param <PK>
 * @param <D>
 */
public interface Node<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends Comparable<Node<PK,D>>{

	DataRouterContext getDataRouterContext();
	DataRouter getRouter();

	String getName();
	Class<PK> getPrimaryKeyType();
	Class<D> getDatabeanType();
	DatabeanFieldInfo<PK,D,?> getFieldInfo();//wildcard the Fielder type so we don't have to put it in the Node's generics (at least for now)
	List<Field<?>> getFields();
	
	List<Field<?>> getNonKeyFields(D d);
	
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
