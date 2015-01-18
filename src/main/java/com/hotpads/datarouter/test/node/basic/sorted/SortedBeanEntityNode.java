package com.hotpads.datarouter.test.node.basic.sorted;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseEntityReaderNode;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTaskNameParams;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.entity.SubEntitySortedMapStorageNode;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseDatarouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean.SortedBeanFielder;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanEntityKey.SortedBeanEntityPartitioner4;

public class SortedBeanEntityNode 
extends HBaseEntityReaderNode<SortedBeanEntityKey,SortedBeanEntity>{
	
//	public static final String
//		NODE_NAME = "SortedBeanEntity",
//		ENTITY_SortedBeanEntity = "SortedBeanEntity";

	
	private static EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity> createEntityNodeParams(){
		EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity> entityNodeParams
			= new EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity>(
			"SortedBeanEntity", SortedBeanEntityKey.class, SortedBeanEntity.class, 
//			SortedBeanEntityPartitioner.class,
			SortedBeanEntityPartitioner4.class,
			"SortedBeanEntity");
		return entityNodeParams;
	}

	private SubEntitySortedMapStorageNode<SortedBeanEntityKey,SortedBeanKey,SortedBean,SortedBeanFielder> sortedBean;
	
	public SortedBeanEntityNode(NodeFactory nodeFactory, Datarouter router, String clientName, String name){
		super(nodeFactory, router, createEntityNodeParams(), new HBaseTaskNameParams(clientName,
				createEntityNodeParams().getEntityTableName(), name));
	}
	
	
	@Override
	protected void initNodes(Datarouter router, String clientName){
		sortedBean = BaseDatarouter.cast(router.register(nodeFactory.subEntityNode(router, entityNodeParams, clientName, 
				SortedBean.class, SortedBeanFielder.class, SortedBeanEntity.QUALIFIER_PREFIX_SortedBean)));
		register(sortedBean);
	}
	
	
	/*********************** get nodes ******************************/

	public SortedMapStorageNode<SortedBeanKey,SortedBean> sortedBean(){
		return sortedBean;
	}
	
	
}
