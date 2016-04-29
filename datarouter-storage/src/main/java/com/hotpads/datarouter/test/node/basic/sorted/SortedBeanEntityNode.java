package com.hotpads.datarouter.test.node.basic.sorted;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.entity.EntityNode;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.entity.SubEntitySortedMapStorageNode;
import com.hotpads.datarouter.node.factory.EntityNodeFactory;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean.SortedBeanFielder;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanEntityKey.SortedBeanEntityPartitioner4;

public class SortedBeanEntityNode{

	public static EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity> ENTITY_NODE_PARAMS_1
			= new EntityNodeParams<>("SortedBeanEntity", SortedBeanEntityKey.class, SortedBeanEntity.class,
			SortedBeanEntityPartitioner4.class, "SortedBeanEntity");

	public static EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity> ENTITY_NODE_PARAMS_2
			= new EntityNodeParams<>("SortedBeanEntity2", SortedBeanEntityKey.class, SortedBeanEntity.class,
			SortedBeanEntityPartitioner4.class, "SortedBeanEntity2");
	public static EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity> ENTITY_NODE_PARAMS_3
			= new EntityNodeParams<>("SortedBeanEntity3", SortedBeanEntityKey.class, SortedBeanEntity.class,
			SortedBeanEntityPartitioner4.class, "SortedBeanEntity3");

	private EntityNode<SortedBeanEntityKey,SortedBeanEntity> entity;
	private SubEntitySortedMapStorageNode<SortedBeanEntityKey,SortedBeanKey,SortedBean,SortedBeanFielder> sortedBean;

	public SortedBeanEntityNode(EntityNodeFactory entityNodeFactory, NodeFactory nodeFactory, Router router,
			ClientId clientId, EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity> entityNodeParams){
		this.entity = entityNodeFactory.create(clientId.getName(), router, entityNodeParams);
		this.sortedBean = router.register(nodeFactory.subEntityNode(router, entityNodeParams, clientId,
				SortedBean.class, SortedBeanFielder.class, SortedBeanEntity.QUALIFIER_PREFIX_SortedBean));
		entity.register(sortedBean);
	}


	/*********************** get nodes ******************************/

	public EntityNode<SortedBeanEntityKey,SortedBeanEntity> entity(){
		return entity;
	}

	public SortedMapStorageNode<SortedBeanKey,SortedBean> sortedBean(){
		return sortedBean;
	}


}
