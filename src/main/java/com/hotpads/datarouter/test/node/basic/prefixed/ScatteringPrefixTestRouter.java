package com.hotpads.datarouter.test.node.basic.prefixed;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.node.op.raw.SortedStorage;
import com.hotpads.datarouter.routing.BaseDatarouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.prefixed.ScatteringPrefixBean.ScatteringPrefixBeanFielder;
import com.hotpads.util.core.ListTool;

@Singleton
public class ScatteringPrefixTestRouter extends BaseDatarouter{

	
	@Inject
	public ScatteringPrefixTestRouter(DatarouterContext drContext, NodeFactory nodeFactory, String clientName){
		super(drContext, DRTestConstants.CONFIG_PATH, ScatteringPrefixTestRouter.class.getSimpleName());
		
		scatteringPrefixBeanNode = register(nodeFactory.create(clientName, 
//					"ScatteringPrefixBean8", ScatteringPrefixBean.class.getName(),//optional test to make sure hbase table naming working
				ScatteringPrefixBean.class, ScatteringPrefixBeanFielder.class, this, false));
		
		registerWithContext();//do after field inits
	}

	/********************************** config **********************************/

	public static final List<ClientId> CLIENT_IDS = ListTool.create(
			new ClientId(DRTestConstants.CLIENT_drTestJdbc0, true),
			new ClientId(DRTestConstants.CLIENT_drTestHibernate0, true),
			new ClientId(DRTestConstants.CLIENT_drTestHBase, true),
			new ClientId(DRTestConstants.CLIENT_drTestMemcached, true));
	
	@Override
	public List<ClientId> getClientIds(){
		return CLIENT_IDS;
	}


	
	/********************************** nodes **********************************/
	
	private Node<ScatteringPrefixBeanKey,ScatteringPrefixBean> scatteringPrefixBeanNode;

	
	/*************************** get/set ***********************************/

	public SortedMapStorage<ScatteringPrefixBeanKey,ScatteringPrefixBean> scatteringPrefixBean(){
		return cast(scatteringPrefixBeanNode);
	}

	public SortedStorage<ScatteringPrefixBeanKey,ScatteringPrefixBean> scatteringPrefixBeanSorted(){
		return cast(scatteringPrefixBeanNode);
	}
	
}





