package com.hotpads.datarouter.test.client;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.connection.keepalive.KeepAlive;
import com.hotpads.datarouter.connection.keepalive.KeepAlive.KeepAliveFielder;
import com.hotpads.datarouter.connection.keepalive.KeepAliveKey;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.client.insert.PutOpTestBean;
import com.hotpads.datarouter.test.client.insert.PutOpTestBean.PutOpTestBeanFielder;
import com.hotpads.datarouter.test.client.insert.PutOpTestBeanKey;
import com.hotpads.datarouter.test.client.pool.PoolTestBean;
import com.hotpads.datarouter.test.client.pool.PoolTestBean.PoolTestBeanFielder;
import com.hotpads.datarouter.test.client.pool.PoolTestBeanKey;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.TxnBean.TxnBeanFielder;
import com.hotpads.datarouter.test.client.txn.TxnBeanKey;
import com.hotpads.util.core.ListTool;

@Singleton
public class BasicClientTestRouterImp
extends BaseDataRouter
implements BasicClientTestRouter{

	public static final String name = "basicClientTest";

	private final NodeFactory nodeFactory;
	
	/********************************** nodes **********************************/

	private MapStorage<KeepAliveKey,KeepAlive> keepAliveHBase;

	private MapStorage<TxnBeanKey,TxnBean> txnBeanJdbc;
	private SortedMapStorageNode<TxnBeanKey,TxnBean> txnBeanHibernate;
	private SortedMapStorage<TxnBeanKey,TxnBean> txnBeanHBase;

	private MapStorage<PoolTestBeanKey,PoolTestBean> poolTestBeanHBase;

	private MapStorage<PutOpTestBeanKey, PutOpTestBean> putOptTest;

	/********************************* constructor *****************************/

	@Inject
	public BasicClientTestRouterImp(DataRouterContext drContext, NodeFactory nodeFactory){
		super(drContext, name);
		this.nodeFactory = nodeFactory;
		keepAliveHBase = cast(register(
				NodeFactory.create(DRTestConstants.CLIENT_drTestHBase, KeepAlive.class, KeepAliveFielder.class, this, false)));

		txnBeanJdbc = cast(register(
				NodeFactory.create(DRTestConstants.CLIENT_drTestJdbc0, TxnBean.class, TxnBeanFielder.class, this, false)));
		txnBeanHibernate = cast(register(
				//note this is not testing hibernate's serialization because we're specifying a fielder
				NodeFactory.create(DRTestConstants.CLIENT_drTestHibernate0, TxnBean.class, TxnBeanFielder.class, this, false)));
		txnBeanHBase = cast(register(
				NodeFactory.create(DRTestConstants.CLIENT_drTestHBase, TxnBean.class, TxnBeanFielder.class, this, false)));

		poolTestBeanHBase = cast(register(
				NodeFactory.create(DRTestConstants.CLIENT_drTestHBase, PoolTestBean.class, PoolTestBeanFielder.class, this, false)));
		
		putOptTest = cast(register(
				NodeFactory.create(DRTestConstants.CLIENT_drTestHibernate0, PutOpTestBean.class, PutOpTestBeanFielder.class, this, false)));
		
		registerWithContext();//do after field inits
	}

	/********************************** config **********************************/

	@Override
	public String getConfigLocation(){
		return DRTestConstants.CONFIG_PATH;
	}

	public static final List<ClientId> CLIENT_IDS = ListTool.create(
			new ClientId(DRTestConstants.CLIENT_drTestJdbc0, true),
			new ClientId(DRTestConstants.CLIENT_drTestHibernate0, true),
			new ClientId(DRTestConstants.CLIENT_drTestHBase, true));
	
	@Override
	public List<ClientId> getClientIds(){
		return CLIENT_IDS;
	}


	/*************************** get/set ***********************************/


	@Override
	public MapStorage<TxnBeanKey,TxnBean> txnBeanJdbc(){
		return txnBeanJdbc;
	}

	@Override
	public SortedMapStorageNode<TxnBeanKey,TxnBean> txnBeanHibernate(){
		return txnBeanHibernate;
	}

	@Override
	public SortedMapStorage<TxnBeanKey,TxnBean> txnBeanHBase(){
		return txnBeanHBase;
	}

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





