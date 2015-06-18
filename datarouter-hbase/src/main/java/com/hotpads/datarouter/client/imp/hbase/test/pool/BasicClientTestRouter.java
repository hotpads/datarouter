package com.hotpads.datarouter.client.imp.hbase.test.pool;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.imp.hbase.test.pool.PoolTestBean.PoolTestBeanFielder;
import com.hotpads.datarouter.connection.keepalive.KeepAlive;
import com.hotpads.datarouter.connection.keepalive.KeepAlive.KeepAliveFielder;
import com.hotpads.datarouter.connection.keepalive.KeepAliveKey;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.routing.BaseDatarouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.util.core.DrListTool;

@Singleton
public class BasicClientTestRouter
extends BaseDatarouter{

	public static final String name = "basicClientTest";

	public static final List<ClientId> CLIENT_IDS = DrListTool.create(
			new ClientId(DRTestConstants.CLIENT_drTestHBase, true));

	
	/********************************** nodes **********************************/

	private final MapStorage<KeepAliveKey,KeepAlive> keepAliveHBase;
	private final MapStorage<PoolTestBeanKey,PoolTestBean> poolTestBeanHBase;


	/********************************* constructor *****************************/

	@Inject
	public BasicClientTestRouter(DatarouterContext drContext, NodeFactory nodeFactory){
		super(drContext, DRTestConstants.CONFIG_PATH, name);
		
		keepAliveHBase = cast(register(nodeFactory.create(DRTestConstants.CLIENT_drTestHBase, KeepAlive.class,
				KeepAliveFielder.class, this, false)));
		poolTestBeanHBase = cast(register(nodeFactory.create(DRTestConstants.CLIENT_drTestHBase, PoolTestBean.class,
				PoolTestBeanFielder.class, this, false)));

		registerWithContext();//do after field inits
	}

	
	/********************************** config **********************************/
	
	@Override
	public List<ClientId> getClientIds(){
		return CLIENT_IDS;
	}


	/*************************** get/set ***********************************/

	public MapStorage<KeepAliveKey,KeepAlive> keepAliveHBase(){
		return keepAliveHBase;
	}

	public MapStorage<PoolTestBeanKey,PoolTestBean> poolTestBeanHBase(){
		return poolTestBeanHBase;
	}

}





