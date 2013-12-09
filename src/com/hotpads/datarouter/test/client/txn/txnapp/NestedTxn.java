/**
 * 
 */
package com.hotpads.datarouter.test.client.txn.txnapp;

import java.util.List;

import org.junit.Assert;

import com.hotpads.datarouter.app.client.parallel.jdbc.base.BaseParallelHibernateTxnApp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientImp;
import com.hotpads.datarouter.client.imp.hibernate.HibernateExecutor;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.connection.ConnectionHandle;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.test.client.BasicClientTestRouter;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.util.core.CollectionTool;

public class NestedTxn extends BaseParallelHibernateTxnApp<Void>{
	
	private DataRouterContext drContext;
	private List<String> clientNames;
	private Isolation isolation;
	private BasicClientTestRouter router;
	private boolean flush;
	
	public NestedTxn(DataRouterContext drContext, List<String> clientNames, Isolation isolation, boolean autoCommit,
			BasicClientTestRouter router, boolean flush){
		super(drContext, clientNames, isolation, autoCommit);
		this.drContext = drContext;
		this.clientNames = clientNames;
		this.isolation = isolation;
		this.router = router;
		this.flush = flush;
	}
	
	@Override
	public Void runOncePerClient(Client client){
		HibernateClientImp hibernateClient = (HibernateClientImp)client;
		ConnectionHandle handle = hibernateClient.getExistingHandle();
		
		TxnBean outer = new TxnBean("outer");
		router.txnBeanHibernate().put(outer, null);
		if(flush){
			getSession(client.getName()).flush();
			getSession(client.getName()).clear();
			List<TxnBean> all = router.txnBeanHibernate().getAll(null);
			Assert.assertEquals(1, CollectionTool.size(all));
		}else{
			List<TxnBean> all = router.txnBeanHibernate().getAll(null);
			if(outer.isFieldAware() || HibernateExecutor.EAGER_SESSION_FLUSH){
				Assert.assertEquals(1, CollectionTool.size(all));
			}else{
				Assert.assertEquals(0, CollectionTool.size(all));
			}
		}
		
		router.run(new InnerTxn(drContext, clientNames, isolation, false, router, true, handle));
		
		TxnBean outer2 = new TxnBean(outer.getId());
		router.txnBeanHibernate().put(outer2, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));//should bust on commit
		return null;
	}
	
	
	public static class InnerTxn extends BaseParallelHibernateTxnApp<Void>{
		private BasicClientTestRouter router;
		private boolean flush;
		private ConnectionHandle outerHandle;

		public InnerTxn(DataRouterContext drContext, List<String> clientNames, Isolation isolation, boolean autoCommit,
				BasicClientTestRouter router, boolean flush, ConnectionHandle outerHandle){
			super(drContext, clientNames, isolation, autoCommit);
			this.router = router;
			this.flush = flush;
			this.outerHandle = outerHandle;
		}
		
		@Override
		public Void runOncePerClient(Client client){
			HibernateClientImp hibernateClient = (HibernateClientImp)client;
			ConnectionHandle handle = hibernateClient.getExistingHandle();
			Assert.assertEquals(outerHandle, handle);
			
			String name = "inner_"+flush;
			TxnBean inner = new TxnBean(name);
			router.txnBeanHibernate().put(inner, null);
			if(flush){
				getSession(client.getName()).flush();
				getSession(client.getName()).clear();
				List<TxnBean> all = router.txnBeanHibernate().getAll(null);
				Assert.assertEquals(2, CollectionTool.size(all));//should not include TxnBean.outer
			}else{
				List<TxnBean> all = router.txnBeanHibernate().getAll(null);
				if(inner.isFieldAware() || HibernateExecutor.EAGER_SESSION_FLUSH){
					Assert.assertEquals(2, CollectionTool.size(all));
				}else{
					Assert.assertEquals(1, CollectionTool.size(all));
				}
			}
			return null;
		}
		
	}
}