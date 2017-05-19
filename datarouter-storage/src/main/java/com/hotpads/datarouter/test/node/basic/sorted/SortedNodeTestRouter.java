package com.hotpads.datarouter.test.node.basic.sorted;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.factory.EntityNodeFactory;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.TestDatarouterProperties;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean.SortedBeanFielder;

public class SortedNodeTestRouter extends BaseRouter{

	private static final String
			NAME = "SortedNodeTestRouter",
			TABLE_NAME_SortedBean = "SortedBean";


	/********************************** nodes **********************************/

	private SortedMapStorageNode<SortedBeanKey,SortedBean> sortedBeanNode;
	private SortedBeanEntityNode sortedBeanEntityNode;

	public SortedNodeTestRouter(TestDatarouterProperties datarouterProperties, Datarouter datarouter,
			DatarouterSettings datarouterSettings, EntityNodeFactory entityNodeFactory,
			EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity> entityNodeParams, NodeFactory nodeFactory,
			ClientId clientId, boolean entity){
		super(datarouter, datarouterProperties.getDatarouterTestFileLocation(), NAME, nodeFactory,
				datarouterSettings);

		String tableName = TABLE_NAME_SortedBean;
		if(entity){
			sortedBeanEntityNode = new SortedBeanEntityNode(entityNodeFactory, nodeFactory, this, clientId,
					entityNodeParams);
			sortedBeanNode = sortedBeanEntityNode.sortedBean();
		}else{
			sortedBeanNode = register(nodeFactory.create(clientId, tableName, SortedBean.class, SortedBeanFielder.class,
					this, false));
		}

	}


	/************************ methods **********************************/

	//we have to do this on-request to avoid trying to cast things like HBaseNode to Indexed Storage
	public IndexedSortedMapStorageNode<SortedBeanKey,SortedBean> indexedSortedBean(){
		return (IndexedSortedMapStorageNode<SortedBeanKey,SortedBean>)sortedBeanNode;
	}


	/*************************** get/set ***********************************/

	public SortedMapStorage<SortedBeanKey,SortedBean> sortedBean(){
		return sortedBeanNode;
	}

	public SortedBeanEntityNode sortedBeanEntity(){
		return sortedBeanEntityNode;
	}
}