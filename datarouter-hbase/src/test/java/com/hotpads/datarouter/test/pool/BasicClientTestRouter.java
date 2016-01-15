package com.hotpads.datarouter.test.pool;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.connection.keepalive.KeepAlive;
import com.hotpads.datarouter.connection.keepalive.KeepAlive.KeepAliveFielder;
import com.hotpads.datarouter.connection.keepalive.KeepAliveKey;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.pool.PoolTestBean.PoolTestBeanFielder;

@Singleton
public class BasicClientTestRouter
extends BaseRouter{

	public static final String name = "basicClientTest";

	public static final List<ClientId> CLIENT_IDS = Collections.singletonList(DrTestConstants.CLIENT_drTestHBase);


	/********************************** nodes **********************************/

	private final SortedMapStorage<KeepAliveKey,KeepAlive> keepAliveHBase;
	private final MapStorage<PoolTestBeanKey,PoolTestBean> poolTestBeanHBase;


	/********************************* constructor *****************************/

	@Inject
	public BasicClientTestRouter(Datarouter datarouter, NodeFactory nodeFactory){
		super(datarouter, DrTestConstants.CONFIG_PATH, name);

		keepAliveHBase = register(nodeFactory.create(DrTestConstants.CLIENT_drTestHBase, KeepAlive.class,
				KeepAliveFielder.class, this, false));
		poolTestBeanHBase = register(nodeFactory.create(DrTestConstants.CLIENT_drTestHBase, PoolTestBean.class,
				PoolTestBeanFielder.class, this, false));

	}


	/********************************** config **********************************/

	@Override
	public List<ClientId> getClientIds(){
		return CLIENT_IDS;
	}


	/*************************** get/set ***********************************/

	public SortedMapStorage<KeepAliveKey,KeepAlive> keepAliveHBase(){
		return keepAliveHBase;
	}

	public MapStorage<PoolTestBeanKey,PoolTestBean> poolTestBeanHBase(){
		return poolTestBeanHBase;
	}
}