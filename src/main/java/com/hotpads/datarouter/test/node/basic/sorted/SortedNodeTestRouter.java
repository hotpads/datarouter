package com.hotpads.datarouter.test.node.basic.sorted;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseDatarouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean.SortedBeanFielder;
import com.hotpads.util.core.ListTool;

@Singleton
public class SortedNodeTestRouter extends BaseDatarouter{

	private static final String 
			name = "basicNodeTest",
			NODE_NAME_SortedBeanEntity = "TestSortedBeanEntity",
			TABLE_NAME_IndexedSortedBean = "IndexedSortedBean",
			ENTITY_NAME_IndexedSortedBean = SortedBean.class.getCanonicalName();

	
	/********************************** nodes **********************************/
	
	private SortedMapStorageNode<SortedBeanKey,SortedBean> sortedBeanNode;
	private SortedBeanEntityNode sortedBeanEntityNode;
	private IndexedSortedMapStorageNode<SortedBeanKey,SortedBean> indexedSortedBean;
	
	
	@Inject
	public SortedNodeTestRouter(DatarouterContext drContext, NodeFactory nodeFactory, String clientName, 
			boolean useFielder, boolean entity){
		super(drContext, DRTestConstants.CONFIG_PATH, name);
		
		if(entity){
			sortedBeanEntityNode = new SortedBeanEntityNode(nodeFactory, this, clientName, NODE_NAME_SortedBeanEntity);
			sortedBeanNode = sortedBeanEntityNode.sortedBean();
		}else{
			if(useFielder){
				sortedBeanNode = cast(register(nodeFactory.create(clientName, SortedBean.class,
						SortedBeanFielder.class, this, false)));
				indexedSortedBean = cast(register(nodeFactory.create(clientName, 
						TABLE_NAME_IndexedSortedBean, ENTITY_NAME_IndexedSortedBean,
						SortedBean.class, SortedBeanFielder.class, this, false)));
			}else{// no fielder to trigger hibernate node
				sortedBeanNode = cast(register(nodeFactory.create(clientName, SortedBean.class, this, false)));
				indexedSortedBean = cast(register(nodeFactory.create(clientName, 
						TABLE_NAME_IndexedSortedBean, ENTITY_NAME_IndexedSortedBean,
						SortedBean.class, this)));
			}
		}
		
		registerWithContext();//do after field inits
	}

	/********************************** config **********************************/

	public static final List<ClientId> CLIENT_IDS = ListTool.create(
			new ClientId(DRTestConstants.CLIENT_drTestJdbc0, true),
			new ClientId(DRTestConstants.CLIENT_drTestHibernate0, true),
			new ClientId(DRTestConstants.CLIENT_drTestHBase, true));
	
	@Override
	public List<ClientId> getClientIds(){
		return CLIENT_IDS;
	}


	
	
	/*************************** get/set ***********************************/

	public SortedMapStorage<SortedBeanKey,SortedBean> sortedBean(){
		return sortedBeanNode;
	}

	public IndexedSortedMapStorage<SortedBeanKey,SortedBean> indexedSortedBean(){
		return indexedSortedBean;
	}
	
	public SortedBeanEntityNode sortedBeanEntity(){
		return sortedBeanEntityNode;
	}

}





