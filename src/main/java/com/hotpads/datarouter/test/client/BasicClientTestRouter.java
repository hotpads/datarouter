package com.hotpads.datarouter.test.client;

import com.google.inject.ImplementedBy;
import com.hotpads.datarouter.connection.keepalive.KeepAlive;
import com.hotpads.datarouter.connection.keepalive.KeepAliveKey;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.test.client.insert.PutOpTestBean;
import com.hotpads.datarouter.test.client.insert.PutOpTestBeanKey;
import com.hotpads.datarouter.test.client.insert.generated.PutOpIdGeneratedTestBean;
import com.hotpads.datarouter.test.client.insert.generated.PutOpIdGeneratedTestBeanKey;
import com.hotpads.datarouter.test.client.pool.PoolTestBean;
import com.hotpads.datarouter.test.client.pool.PoolTestBeanKey;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.TxnBeanKey;

@ImplementedBy(BasicClientTestRouterImp.class)
public interface BasicClientTestRouter extends DataRouter{

	MapStorage<TxnBeanKey,TxnBean> txnBeanJdbc();
	SortedMapStorageNode<TxnBeanKey,TxnBean> txnBeanHibernate();
	SortedMapStorage<TxnBeanKey,TxnBean> txnBeanHBase();

	MapStorage<KeepAliveKey,KeepAlive> keepAliveHBase();

	MapStorage<PoolTestBeanKey,PoolTestBean> poolTestBeanHBase();
	
	MapStorage<PutOpTestBeanKey, PutOpTestBean> putOptTest();
	
	IndexedSortedMapStorage<PutOpIdGeneratedTestBeanKey, PutOpIdGeneratedTestBean> getPutOpIdGeneratedTest();

}