package com.hotpads.datarouter.test.node.basic.sorted;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseEntityReaderNode;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTaskNameParams;
import com.hotpads.datarouter.node.entity.SubEntitySortedMapStorageNode;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean.SortedBeanFielder;

public class SortedBeanEntityNode 
extends HBaseEntityReaderNode<SortedBeanEntityKey,SortedBeanEntity>{
	
	public static final String
		NODE_NAME = "SortedBeanEntity",
		ENTITY_SortedBeanEntity = "SortedBeanEntity";

	private SubEntitySortedMapStorageNode<SortedBeanEntityKey,SortedBeanKey,SortedBean,SortedBeanFielder> sortedBean;
	
	public SortedBeanEntityNode(DataRouter router, String clientName, String name){
		super(router, new HBaseTaskNameParams(clientName, ENTITY_SortedBeanEntity, name));
	}
	
	
	@Override
	protected void initNodes(DataRouter router, String clientName){
		sortedBean = BaseDataRouter.cast(NodeFactory.subEntityNode(router, clientName, NODE_NAME,
				SortedBeanEntityKey.class, SortedBean.class, SortedBeanFielder.class,
				SortedBeanEntity.class, ENTITY_SortedBeanEntity, SortedBeanEntity.QUALIFIER_PREFIX_SortedBean));
		register(sortedBean);
	}
	
	
	/*********************** get nodes ******************************/

	public SortedMapStorageNode<SortedBeanKey,SortedBean> sortedBean(){
		return sortedBean;
	}
	
	
}
