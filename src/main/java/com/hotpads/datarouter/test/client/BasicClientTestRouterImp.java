package com.hotpads.datarouter.test.client;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.connection.keepalive.KeepAlive;
import com.hotpads.datarouter.connection.keepalive.KeepAlive.KeepAliveFielder;
import com.hotpads.datarouter.connection.keepalive.KeepAliveKey;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.routing.BaseDatarouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.client.insert.PutOpTestBean;
import com.hotpads.datarouter.test.client.insert.PutOpTestBean.PutOpTestBeanFielder;
import com.hotpads.datarouter.test.client.insert.PutOpTestBeanKey;
import com.hotpads.datarouter.test.client.pool.PoolTestBean;
import com.hotpads.datarouter.test.client.pool.PoolTestBean.PoolTestBeanFielder;
import com.hotpads.datarouter.test.client.pool.PoolTestBeanKey;
import com.hotpads.datarouter.util.core.DrListTool;

@Singleton
public class BasicClientTestRouterImp
extends BaseDatarouter
implements BasicClientTestRouter{

	public static final String name = "basicClientTest";

	/********************************** nodes **********************************/

	private MapStorage<KeepAliveKey,KeepAlive> keepAliveHBase;

	private MapStorage<PoolTestBeanKey,PoolTestBean> poolTestBeanHBase;

	private MapStorage<PutOpTestBeanKey, PutOpTestBean> putOptTest;

	/********************************* constructor *****************************/

	@Inject
	public BasicClientTestRouterImp(DatarouterContext drContext, NodeFactory nodeFactory){
		super(drContext, DRTestConstants.CONFIG_PATH, name);
		keepAliveHBase = cast(register(
				nodeFactory.create(DRTestConstants.CLIENT_drTestHBase, KeepAlive.class, KeepAliveFielder.class, this, false)));

		poolTestBeanHBase = cast(register(
				nodeFactory.create(DRTestConstants.CLIENT_drTestHBase, PoolTestBean.class, PoolTestBeanFielder.class, this, false)));
		
		putOptTest = cast(register(
				nodeFactory.create(DRTestConstants.CLIENT_drTestJdbc0, PutOpTestBean.class, PutOpTestBeanFielder.class, this, false)));
		
		registerWithContext();//do after field inits
	}

	/********************************** config **********************************/

	public static final List<ClientId> CLIENT_IDS = DrListTool.create(
			new ClientId(DRTestConstants.CLIENT_drTestJdbc0, true),
			new ClientId(DRTestConstants.CLIENT_drTestHibernate0, true),
			new ClientId(DRTestConstants.CLIENT_drTestHBase, true));
	
	@Override
	public List<ClientId> getClientIds(){
		return CLIENT_IDS;
	}


	/*************************** get/set ***********************************/

	@Override
	public MapStorage<KeepAliveKey,KeepAlive> keepAliveHBase(){
		return keepAliveHBase;
	}

	@Override
	public MapStorage<PoolTestBeanKey,PoolTestBean> poolTestBeanHBase(){
		return poolTestBeanHBase;
	}

	@Override
	public MapStorage<PutOpTestBeanKey, PutOpTestBean> putOptTest(){
		return putOptTest;
	}

}





