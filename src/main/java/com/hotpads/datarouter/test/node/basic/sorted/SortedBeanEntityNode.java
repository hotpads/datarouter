package com.hotpads.datarouter.test.node.basic.sorted;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseEntityReaderNode;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTaskNameParams;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.entity.SubEntitySortedMapStorageNode;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean.SortedBeanFielder;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanEntityKey.SortedBeanEntityPartitioner4;

public class SortedBeanEntityNode 
extends HBaseEntityReaderNode<SortedBeanEntityKey,SortedBeanEntity>{
	
//	public static final String
//		NODE_NAME = "SortedBeanEntity",
//		ENTITY_SortedBeanEntity = "SortedBeanEntity";

	
	private static final EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity> entityNodeParams
			= new EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity>(
			"SortedBeanEntity", SortedBeanEntityKey.class, SortedBeanEntity.class, 
//			SortedBeanEntityPartitioner.class,
			SortedBeanEntityPartitioner4.class,
			"SortedBeanEntity");

	private SubEntitySortedMapStorageNode<SortedBeanEntityKey,SortedBeanKey,SortedBean,SortedBeanFielder> sortedBean;
	
	public SortedBeanEntityNode(DataRouter router, String clientName, String name){
		super(router, entityNodeParams, new HBaseTaskNameParams(clientName, entityNodeParams.getEntityTableName(), name));
	}
	
	
	@Override
	protected void initNodes(DataRouter router, String clientName){
		sortedBean = BaseDataRouter.cast(router.register(NodeFactory.subEntityNode(router, entityNodeParams, clientName, 
				entityNodeParams.getNodeName(), SortedBean.class, SortedBeanFielder.class,
				entityNodeParams.getEntityTableName(), SortedBeanEntity.QUALIFIER_PREFIX_SortedBean)));
		register(sortedBean);
	}
	
	
	/*********************** get nodes ******************************/

	public SortedMapStorageNode<SortedBeanKey,SortedBean> sortedBean(){
		return sortedBean;
	}
	
	
}
