package com.hotpads.datarouter.test.node.basic.sorted;

import java.util.ArrayList;
import java.util.List;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.EntityNodeFactory;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean.SortedBeanFielder;

public class SortedNodeTestRouter extends BaseRouter{

	private final List<ClientId> clientIds;

	private static final String
			NAME = "SortedNodeTestRouter",
			TABLE_NAME_SortedBean = "SortedBean";


	/********************************** nodes **********************************/

	private SortedMapStorageNode<SortedBeanKey,SortedBean> sortedBeanNode;
	private SortedBeanEntityNode sortedBeanEntityNode;


	public SortedNodeTestRouter(Datarouter datarouter, EntityNodeFactory entityNodeFactory,
			NodeFactory nodeFactory, ClientId clientId, boolean useFielder, boolean entity){
		super(datarouter, DrTestConstants.CONFIG_PATH, NAME);

		this.clientIds = new ArrayList<>();
		this.clientIds.add(clientId);

		String tableName = TABLE_NAME_SortedBean;
		String entityName = SortedBean.class.getCanonicalName();
		if(entity){
			sortedBeanEntityNode = new SortedBeanEntityNode(entityNodeFactory, nodeFactory, this, clientId);
			sortedBeanNode = sortedBeanEntityNode.sortedBean();
		}else{
			if(useFielder){
				sortedBeanNode = register(nodeFactory.create(clientId, tableName, entityName, SortedBean.class,
						SortedBeanFielder.class, this, false));
			}else{// no fielder to trigger hibernate node
				sortedBeanNode = register(nodeFactory.create(clientId, SortedBean.class, this, false));
			}
		}

	}

	/********************************** config **********************************/

	@Override
	public List<ClientId> getClientIds(){
		return clientIds;
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