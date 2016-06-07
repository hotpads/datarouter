 package com.hotpads.datarouter.node;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

/**
 * A Node is the interface through which the application sends Databeans for serialization and storage. It ties together
 * a PrimaryKey type, a Databean type, a Fielder type. A Node can be a PhysicalNode or a virtual node, like
 * MasterSlaveNode, that forwards requests on to other nodes.
 */
public interface Node<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends Comparable<Node<PK,D>>{

	Datarouter getDatarouter();
	Router getRouter();

	@Deprecated //maybe we should remove this as it's only used once in Nodes.java
	boolean isPhysicalNodeOrWrapper();
	//used by a couple places that need access to methods on the wrapped node
	PhysicalNode<PK,D> getPhysicalNodeIfApplicable();

	String getName();
	Class<PK> getPrimaryKeyType();
	//wildcard the Fielder type so we don't have to put it in the Node's generics (at least for now)
	DatabeanFieldInfo<PK,D,?> getFieldInfo();
	List<Field<?>> getFields();

	List<Field<?>> getNonKeyFields(D databean);

	Set<String> getAllNames();
	List<String> getClientNames();
	List<ClientId> getClientIds();
	boolean usesClient(String clientName);
	List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys);
	List<? extends PhysicalNode<PK,D>> getPhysicalNodes();
	List<? extends PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName);
	Node<PK,D> getMaster();
	List<? extends Node<PK,D>> getChildNodes();

}
