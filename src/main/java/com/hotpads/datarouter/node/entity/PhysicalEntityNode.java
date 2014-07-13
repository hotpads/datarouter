package com.hotpads.datarouter.node.entity;

import java.util.Map;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;

public interface PhysicalEntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>
extends EntityNode<EK,E>{

	String getClientName();
	String getTableName();

	Map<String,? extends SubEntitySortedMapStorageReaderNode<EK,?,?,?>> getNodeByQualifierPrefix();
	
	Client getClient();
	
}
