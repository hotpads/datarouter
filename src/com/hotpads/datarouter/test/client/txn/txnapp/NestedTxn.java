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
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.connection.ConnectionHandle;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.client.BasicClientTestRouter;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class NestedTxn extends BaseParallelHibernateTxnApp<Void>{
	BasicClientTestRouter router;
	boolean flush;
	
	public NestedTxn(BasicClientTestRouter router, boolean flush){
		super(router);
		this.router = router;
		this.flush = flush;
	}
	
	@Override
	public List<String> getClientNames(){
		return ListTool.wrap(DRTestConstants.CLIENT_drTestHibernate0);
	}
	
	@Override
	public Void runOncePerClient(Client client){
		HibernateClientImp hibernateClient = (HibernateClientImp)client;
		ConnectionHandle handle = hibernateClient.getExistingHandle();
		
		TxnBean outer = new TxnBean("outer");
		router.txnBean().put(outer, null);
		if(flush){
			this.getSession(client.getName()).flush();
			this.getSession(client.getName()).clear();
			List<TxnBean> all = router.txnBean().getAll(null);
			Assert.assertEquals(1, CollectionTool.size(all));
		}else{
			List<TxnBean> all = router.txnBean().getAll(null);
			if(outer.isFieldAware() || HibernateExecutor.EAGER_SESSION_FLUSH){
				Assert.assertEquals(1, CollectionTool.size(all));
			}else{
				Assert.assertEquals(0, CollectionTool.size(all));
			}
		}
		
		router.run(new InnerTxn(router, true, handle));
		
		TxnBean outer2 = new TxnBean(outer.getId());
		router.txnBean().put(outer2, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));//should bust on commit
		return null;
	}
	
	
	public static class InnerTxn extends BaseParallelHibernateTxnApp<Void>{
		BasicClientTestRouter router;
		boolean flush;
		ConnectionHandle outerHandle;

		public InnerTxn(BasicClientTestRouter router, boolean flush, ConnectionHandle outerHandle){
			super(router);
			this.router = router;
			this.flush = flush;
			this.outerHandle = outerHandle;
		}
		
		@Override
		public List<String> getClientNames(){
			return ListTool.wrap(DRTestConstants.CLIENT_drTestHibernate0);
		}
		
		@Override
		public Void runOncePerClient(Client client){
			HibernateClientImp hibernateClient = (HibernateClientImp)client;
			ConnectionHandle handle = hibernateClient.getExistingHandle();
			Assert.assertEquals(outerHandle, handle);
			
			String name = "inner_"+flush;
			TxnBean inner = new TxnBean(name);
			router.txnBean().put(inner, null);
			if(flush){
				this.getSession(client.getName()).flush();
				this.getSession(client.getName()).clear();
				List<TxnBean> all = router.txnBean().getAll(null);
				Assert.assertEquals(2, CollectionTool.size(all));//should not include TxnBean.outer
			}else{
				List<TxnBean> all = router.txnBean().getAll(null);
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