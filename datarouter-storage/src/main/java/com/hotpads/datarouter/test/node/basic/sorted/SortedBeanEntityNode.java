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

	private static EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity> entityNodeParams
			= new EntityNodeParams<>("SortedBeanEntity", SortedBeanEntityKey.class, SortedBeanEntity.class,
			SortedBeanEntityPartitioner4.class, "SortedBeanEntity");

	private EntityNode<SortedBeanEntityKey,SortedBeanEntity> entity;
	private SubEntitySortedMapStorageNode<SortedBeanEntityKey,SortedBeanKey,SortedBean,SortedBeanFielder> sortedBean;

	public SortedBeanEntityNode(EntityNodeFactory entityNodeFactory, NodeFactory nodeFactory, Router router,
			ClientId clientId){
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
