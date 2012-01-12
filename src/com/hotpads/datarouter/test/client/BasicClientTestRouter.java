package com.hotpads.datarouter.test.client;

import com.google.inject.ImplementedBy;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.TxnBeanKey;

@ImplementedBy(BasicClientTestRouterImp.class)
public interface BasicClientTestRouter extends DataRouter{

	MapStorage<TxnBeanKey,TxnBean> txnBeanHibernate();
	MapStorage<TxnBeanKey,TxnBean> txnBeanHBase();

	
	
}