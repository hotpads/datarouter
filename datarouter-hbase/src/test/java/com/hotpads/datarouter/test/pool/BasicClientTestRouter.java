package com.hotpads.datarouter.test.pool;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.connection.keepalive.KeepAlive;
import com.hotpads.datarouter.connection.keepalive.KeepAlive.KeepAliveFielder;
import com.hotpads.datarouter.connection.keepalive.KeepAliveKey;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.TestDatarouterProperties;
import com.hotpads.datarouter.test.pool.PoolTestBean.PoolTestBeanFielder;

@Singleton
public class BasicClientTestRouter
extends BaseRouter{

	public static final String name = "basicClientTest";


	/********************************** nodes **********************************/

	private final SortedMapStorage<KeepAliveKey,KeepAlive> keepAliveHBase;
	private final MapStorage<PoolTestBeanKey,PoolTestBean> poolTestBeanHBase;


	/********************************* constructor *****************************/

	@Inject
	public BasicClientTestRouter(TestDatarouterProperties datarouterProperties, Datarouter datarouter,
			DatarouterSettings datarouterSettings, NodeFactory nodeFactory){
		super(datarouter, datarouterProperties.getTestRouterConfigFileLocation(), name, nodeFactory,
				datarouterSettings);

		keepAliveHBase = register(nodeFactory.create(DrTestConstants.CLIENT_drTestHBase, KeepAlive.class,
				KeepAliveFielder.class, this, false));
		poolTestBeanHBase = register(nodeFactory.create(DrTestConstants.CLIENT_drTestHBase, PoolTestBean.class,
				PoolTestBeanFielder.class, this, false));

	}


	/*************************** get/set ***********************************/

	public SortedMapStorage<KeepAliveKey,KeepAlive> keepAliveHBase(){
		return keepAliveHBase;
	}

	public MapStorage<PoolTestBeanKey,PoolTestBean> poolTestBeanHBase(){
		return poolTestBeanHBase;
	}
}