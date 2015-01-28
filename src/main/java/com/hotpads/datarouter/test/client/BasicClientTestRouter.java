package com.hotpads.datarouter.test.client;

import com.google.inject.ImplementedBy;
import com.hotpads.datarouter.connection.keepalive.KeepAlive;
import com.hotpads.datarouter.connection.keepalive.KeepAliveKey;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.client.insert.PutOpTestBean;
import com.hotpads.datarouter.test.client.insert.PutOpTestBeanKey;
import com.hotpads.datarouter.test.client.pool.PoolTestBean;
import com.hotpads.datarouter.test.client.pool.PoolTestBeanKey;

@ImplementedBy(BasicClientTestRouterImp.class)
public interface BasicClientTestRouter extends Datarouter{

	MapStorage<KeepAliveKey,KeepAlive> keepAliveHBase();

	MapStorage<PoolTestBeanKey,PoolTestBean> poolTestBeanHBase();
	
	MapStorage<PutOpTestBeanKey, PutOpTestBean> putOptTest();
	
}