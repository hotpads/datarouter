package com.hotpads.datarouter.test.client.insert;

import java.util.Arrays;
import java.util.List;

import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.client.insert.PutOpTestBean.PutOpTestBeanFielder;

@Singleton
public class PutOpTestRouter
extends BaseRouter{

	public static final String name = "PutOpTest";


	/********************************** nodes **********************************/

	private MapStorage<PutOpTestBeanKey, PutOpTestBean> putOptTest;

	/********************************* constructor *****************************/

	public PutOpTestRouter(Datarouter datarouter, NodeFactory nodeFactory, ClientId clientId){
		super(datarouter, DrTestConstants.CONFIG_PATH, name);

		this.putOptTest = register(nodeFactory.create(clientId, PutOpTestBean.class, PutOpTestBeanFielder.class, this,
				false));

	}

	/********************************** config **********************************/

	@Override
	public List<ClientId> getClientIds(){
		return Arrays.asList(DrTestConstants.CLIENT_drTestJdbc0);
	}


	/*************************** get/set ***********************************/

	public MapStorage<PutOpTestBeanKey, PutOpTestBean> putOptTest(){
		return putOptTest;
	}

}