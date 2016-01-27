package com.hotpads.datarouter.node.entity;

import java.util.HashMap;
import java.util.Map;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientTableNodeNames;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.SortedMapStorageReaderNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fieldcache.EntityFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;

public abstract class BasePhysicalEntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>
extends BaseEntityNode<EK,E>
implements PhysicalEntityNode<EK,E>{

//	private EntityNodeParams<EK,E> entityNodeParams;
	protected EntityFieldInfo<EK,E> entityFieldInfo;
	private ClientTableNodeNames clientTableNodeNames;//currently acting as a cache of superclass fields
	private Map<String,SubEntitySortedMapStorageReaderNode<EK,?,?,?>> nodeByQualifierPrefix;
	
	
	public BasePhysicalEntityNode(Datarouter datarouter, EntityNodeParams<EK,E> entityNodeParams,
			ClientTableNodeNames clientTableNodeNames){
		super(datarouter, clientTableNodeNames.getNodeName());
//		this.entityNodeParams = entityNodeParams;
		this.entityFieldInfo = new EntityFieldInfo<>(entityNodeParams);
		this.clientTableNodeNames = clientTableNodeNames;
		this.nodeByQualifierPrefix = new HashMap<>();
	}

	@Override
	public <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>> void register(
			SortedMapStorageReaderNode<PK,D> node){
		super.register(node);
		SubEntitySortedMapStorageReaderNode<EK,PK,D,?> subEntityNode = (SubEntitySortedMapStorageReaderNode<EK,PK,D,?>)
				node;
		nodeByQualifierPrefix.put(subEntityNode.getEntityNodePrefix(), subEntityNode);
	}

	
	@Override
	public Client getClient(){
		return getContext().getClientPool().getClient(getClientName());
	}

	@Override
	public String getClientName(){
		return clientTableNodeNames.getClientName();
	}

	@Override
	public String getTableName(){
		return clientTableNodeNames.getTableName();
	}
	
	@Override
	public Map<String,? extends SubEntitySortedMapStorageReaderNode<EK,?,?,?>> getNodeByQualifierPrefix(){
		return nodeByQualifierPrefix;
	}
	
//	public EntityNodeParams<EK,E> getEntityNodeParams(){
//		return entityNodeParams;
//	}
	public EntityFieldInfo<EK,E> getEntityFieldInfo(){
		return entityFieldInfo;
	}
	
}
