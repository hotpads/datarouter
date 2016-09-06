package com.hotpads.datarouter.test.node.basic.map;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.entity.EntityNode;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.entity.SubEntitySortedMapStorageNode;
import com.hotpads.datarouter.node.factory.EntityNodeFactory;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBean;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBean.MapStorageBeanFielder;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBeanEntity;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBeanEntityKey;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBeanEntityKey.MapStorageBeanEntityPartitioner;
import com.hotpads.datarouter.test.node.basic.map.databean.MapStorageBeanKey;

public class MapStorageEntityNode{

	public static EntityNodeParams<MapStorageBeanEntityKey,MapStorageBeanEntity> ENTITY_NODE_PARAMS_1 =
			new EntityNodeParams<>("MapStorageBeanEntity", MapStorageBeanEntityKey.class, MapStorageBeanEntity.class,
			MapStorageBeanEntityPartitioner.class, "MapStorageBeanEntity");

	private EntityNode<MapStorageBeanEntityKey,MapStorageBeanEntity> entity;

	private SubEntitySortedMapStorageNode
			<MapStorageBeanEntityKey,MapStorageBeanKey,MapStorageBean,MapStorageBeanFielder> mapStorageNode;

	public MapStorageEntityNode(EntityNodeFactory entityNodeFactory, NodeFactory nodeFactory, Router router,
			ClientId clientId, EntityNodeParams<MapStorageBeanEntityKey,MapStorageBeanEntity> entityNodeParams){
		this.entity = entityNodeFactory.create(clientId.getName(), router, entityNodeParams);
		this.mapStorageNode = router.register(nodeFactory.subEntityNode(router, entityNodeParams, clientId,
				MapStorageBean::new, MapStorageBeanFielder::new, MapStorageBeanEntity.QUALIFIER_PREFIX_MapStorageBean));
		entity.register(mapStorageNode);
	}

	/*********************** get nodes ******************************/

	public EntityNode<MapStorageBeanEntityKey,MapStorageBeanEntity> entity(){
		return entity;
	}

	public MapStorageNode<MapStorageBeanKey,MapStorageBean> mapStorageNode(){
		return mapStorageNode;
	}
}