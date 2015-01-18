package com.hotpads.datarouter.test.node.basic.sorted;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
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
			TABLE_NAME_SortedBean = "SortedBean",
			NODE_NAME_SortedBeanEntity = "TestSortedBeanEntity",
			ENTITY_NAME_SortedBean = SortedBean.class.getCanonicalName();

	
	/********************************** nodes **********************************/
	
	private SortedMapStorageNode<SortedBeanKey,SortedBean> sortedBeanNode;
	private SortedBeanEntityNode sortedBeanEntityNode;
	
	
	@Inject
	public SortedNodeTestRouter(DatarouterContext drContext, NodeFactory nodeFactory, String clientName, 
			boolean useFielder, boolean entity){
		super(drContext, DRTestConstants.CONFIG_PATH, name);
		
		String tableName = TABLE_NAME_SortedBean;
//		String entityName = SortedBean.class.getPackage().getName() + "." + tableName;
		String entityName = SortedBean.class.getCanonicalName();
		if(entity){
			sortedBeanEntityNode = new SortedBeanEntityNode(nodeFactory, this, clientName, NODE_NAME_SortedBeanEntity);
			sortedBeanNode = sortedBeanEntityNode.sortedBean();
		}else{
			if(useFielder){
				sortedBeanNode = cast(register(nodeFactory.create(clientName, 
						tableName, entityName,
						SortedBean.class, SortedBeanFielder.class, this, false)));
			}else{// no fielder to trigger hibernate node
				sortedBeanNode = cast(register(nodeFactory.create(clientName, 
//						tableName, entityName,
						SortedBean.class, this, false)));
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


	/************************ methods **********************************/
	
	//we have to do this on-request to avoid trying to cast things like HBaseNode to Indexed Storage
	public IndexedSortedMapStorageNode<SortedBeanKey,SortedBean> indexedSortedBean(){
		return cast(sortedBeanNode);
	}
	
	
	/*************************** get/set ***********************************/

	public SortedMapStorage<SortedBeanKey,SortedBean> sortedBean(){
		return sortedBeanNode;
	}
	
	public SortedBeanEntityNode sortedBeanEntity(){
		return sortedBeanEntityNode;
	}

}





