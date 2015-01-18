package com.hotpads.datarouter.test.node.basic.sorted;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseDatarouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean.SortedBeanFielder;
import com.hotpads.util.core.ListTool;

@Singleton
public class IndexedNodeTestRouter extends BaseDatarouter{

	private static final String 
			name = "basicNodeTest",
			TABLE_NAME_IndexedSortedBean = "IndexedSortedBean",
			ENTITY_NAME_SortedBean = SortedBean.class.getCanonicalName();

	
	/********************************** nodes **********************************/
	
	private IndexedSortedMapStorageNode<SortedBeanKey,SortedBean> indexedSortedBean;
	
	
	@Inject
	public IndexedNodeTestRouter(DatarouterContext drContext, NodeFactory nodeFactory, String clientName, 
			boolean useFielder, boolean entity, String tableSuffix){
		super(drContext, DRTestConstants.CONFIG_PATH, name);
		
			if(useFielder){
				indexedSortedBean = cast(register(nodeFactory.create(clientName, 
						TABLE_NAME_IndexedSortedBean + tableSuffix, ENTITY_NAME_SortedBean,
						SortedBean.class, SortedBeanFielder.class, this, false)));
			}else{// no fielder to trigger hibernate node
				indexedSortedBean = cast(register(nodeFactory.create(clientName, 
						TABLE_NAME_IndexedSortedBean + tableSuffix, ENTITY_NAME_SortedBean,
						SortedBean.class, this)));
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


	public IndexedSortedMapStorage<SortedBeanKey,SortedBean> indexedSortedBean(){
		return indexedSortedBean;
	}
	

}





