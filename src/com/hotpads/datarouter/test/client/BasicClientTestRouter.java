package com.hotpads.datarouter.test.client;

import com.google.inject.ImplementedBy;
import com.hotpads.datarouter.node.op.MapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.TxnBeanKey;

@ImplementedBy(HibernateBasicClientTestRouter.class)
public interface BasicClientTestRouter extends DataRouter{

	MapStorageNode<TxnBeanKey,TxnBean> txnBean();

	
	
}