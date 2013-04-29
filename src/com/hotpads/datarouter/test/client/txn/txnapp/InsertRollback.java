/**
 * 
 */
package com.hotpads.datarouter.test.client.txn.txnapp;

import java.util.List;

import org.junit.Assert;

import com.hotpads.datarouter.app.client.parallel.jdbc.base.BaseParallelHibernateTxnApp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.HibernateExecutor;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.test.client.BasicClientTestRouter;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.util.core.CollectionTool;

public class InsertRollback extends BaseParallelHibernateTxnApp<Void>{
	
	private BasicClientTestRouter router;
	private boolean flush;
	
	public InsertRollback(DataRouterContext drContext, List<String> clientNames, Isolation isolation,
			BasicClientTestRouter router, boolean flush){
		super(drContext, clientNames, isolation);
		this.router = router;
		this.flush = flush;
	}
	
	@Override
	public Void runOncePerClient(Client client){
		TxnBean a = new TxnBean("a");
		router.txnBeanHibernate().put(a, null);
		if(flush){
			this.getSession(client.getName()).flush();
			this.getSession(client.getName()).clear();
			Assert.assertEquals(1, CollectionTool.size(router.txnBeanHibernate().getAll(null)));
		}else{
			if(a.isFieldAware() || HibernateExecutor.EAGER_SESSION_FLUSH){
				Assert.assertEquals(1, CollectionTool.size(router.txnBeanHibernate().getAll(null)));
			}else{
				Assert.assertEquals(0, CollectionTool.size(router.txnBeanHibernate().getAll(null)));
			}
		}
		TxnBean a2 = new TxnBean("a2");
		a2.setId(a.getId());
		router.txnBeanHibernate().put(a2, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));//should bust on commit
		return null;
	}
}