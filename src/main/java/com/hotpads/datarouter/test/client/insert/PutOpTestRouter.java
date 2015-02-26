package com.hotpads.datarouter.test.client.insert;

import java.util.List;

import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.routing.BaseDatarouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.client.insert.PutOpTestBean.PutOpTestBeanFielder;
import com.hotpads.datarouter.util.core.ListTool;

@Singleton
public class PutOpTestRouter
extends BaseDatarouter
{

	public static final String name = "PutOpTest";
	
	private String clientName;

	/********************************** nodes **********************************/

	private MapStorage<PutOpTestBeanKey, PutOpTestBean> putOptTest;

	/********************************* constructor *****************************/

	public PutOpTestRouter(DatarouterContext drContext, NodeFactory nodeFactory, String clientName){
		super(drContext, DRTestConstants.CONFIG_PATH, name);
		this.clientName = clientName;
		
		this.putOptTest = cast(register(nodeFactory.create(clientName, PutOpTestBean.class, PutOpTestBeanFielder.class,
				this, false)));
		
		registerWithContext();//do after field inits
	}

	/********************************** config **********************************/

	@Override
	public List<ClientId> getClientIds(){
		return ListTool.create(
				new ClientId(DRTestConstants.CLIENT_drTestJdbc0, true));
	}


	/*************************** get/set ***********************************/

	public MapStorage<PutOpTestBeanKey, PutOpTestBean> putOptTest(){
		return putOptTest;
	}

}





