package com.hotpads.datarouter.test.client.insert;

import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.TestDatarouterProperties;
import com.hotpads.datarouter.test.client.insert.PutOpTestBean.PutOpTestBeanFielder;

@Singleton
public class PutOpTestRouter
extends BaseRouter{

	public static final String name = "PutOpTest";


	/********************************** nodes **********************************/

	private MapStorage<PutOpTestBeanKey, PutOpTestBean> putOptTest;

	/********************************* constructor *****************************/

	public PutOpTestRouter(TestDatarouterProperties datarouterProperties, Datarouter datarouter,
			DatarouterSettings datarouterSettings, NodeFactory nodeFactory, ClientId clientId){
		super(datarouter, datarouterProperties.getDatarouterTestFileLocation(), name, nodeFactory,
				datarouterSettings);

		this.putOptTest = register(nodeFactory.create(clientId, PutOpTestBean.class, PutOpTestBeanFielder.class, this,
				false));

	}


	/*************************** get/set ***********************************/

	public MapStorage<PutOpTestBeanKey, PutOpTestBean> putOptTest(){
		return putOptTest;
	}

}