package com.hotpads.datarouter.test.node.basic.manyfield;

import java.util.List;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.BaseDatarouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldBean.ManyFieldTypeBeanFielder;
import com.hotpads.datarouter.util.core.DrListTool;

@Singleton
public class ManyFieldTestRouter extends BaseDatarouter{

	@Inject
	public ManyFieldTestRouter(DatarouterContext drContext, NodeFactory nodeFactory, String clientName,
			boolean useFielder){
		super(drContext, DRTestConstants.CONFIG_PATH, ManyFieldTestRouter.class.getSimpleName());
		
		Class<ManyFieldTypeBeanFielder> fielderClass = useFielder ? ManyFieldTypeBeanFielder.class : null;
		this.manyFieldTypeBeanNode = register(nodeFactory.create(clientName, ManyFieldBean.class, fielderClass, 
				new Random().nextInt(), this, false));
		
		registerWithContext();//do after field inits
	}

	/********************************** config **********************************/

	public static final List<ClientId> CLIENT_IDS = DrListTool.create(
			new ClientId(DRTestConstants.CLIENT_drTestMemory, true),
			new ClientId(DRTestConstants.CLIENT_drTestJdbc0, true),
			new ClientId(DRTestConstants.CLIENT_drTestHibernate0, true),
			new ClientId(DRTestConstants.CLIENT_drTestHBase, true),
			new ClientId(DRTestConstants.CLIENT_drTestMemcached, true));
	
	@Override
	public List<ClientId> getClientIds(){
		return CLIENT_IDS;
	}


	
	/********************************** nodes **********************************/
	
	protected Node<ManyFieldBeanKey,ManyFieldBean> manyFieldTypeBeanNode;

	
	/*************************** get/set ***********************************/

	public MapStorageNode<ManyFieldBeanKey,ManyFieldBean> manyFieldTypeBean() {
		return cast(manyFieldTypeBeanNode);
	}

}





