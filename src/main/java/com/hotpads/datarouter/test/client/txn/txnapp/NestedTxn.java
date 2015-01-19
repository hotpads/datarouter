/**
 * 
 */
package com.hotpads.datarouter.test.client.txn.txnapp;

import java.util.List;

import org.junit.Assert;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientImp;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.connection.ConnectionHandle;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.TxnBeanKey;
import com.hotpads.datarouter.test.client.txn.TxnTestRouter;

public class NestedTxn extends BaseHibernateOp<Void>{
	
	private DatarouterContext drContext;
	private List<String> clientNames;
	private Isolation isolation;
	private TxnTestRouter router;
	private boolean flush;
	
	public NestedTxn(DatarouterContext drContext, List<String> clientNames, Isolation isolation, boolean autoCommit,
			TxnTestRouter router, boolean flush){
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
		router.txnBean().put(outer, null);
		if(flush){
			getSession(client.getName()).flush();
			getSession(client.getName()).clear();
			Assert.assertTrue(router.txnBean().exists(outer.getKey(), null));
		}else{
			boolean fieldAware = router.txnBean().getFieldInfo().getFieldAware();
			if(fieldAware || SessionExecutorImpl.EAGER_SESSION_FLUSH){
				Assert.assertTrue(router.txnBean().exists(outer.getKey(), null));
			}else{
				Assert.assertFalse(router.txnBean().exists(outer.getKey(), null));
			}
		}
		
		router.run(new InnerTxn(drContext, clientNames, isolation, false, router, true, handle));
		
		TxnBean outer2 = new TxnBean(outer.getId());
		router.txnBean().put(outer2, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));//should bust on commit
		return null;
	}
	
	
	public static class InnerTxn extends BaseHibernateOp<Void>{
		private TxnTestRouter router;
		private boolean flush;
		private ConnectionHandle outerHandle;

		public InnerTxn(DatarouterContext drContext, List<String> clientNames, Isolation isolation, boolean autoCommit,
				TxnTestRouter router, boolean flush, ConnectionHandle outerHandle){
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
			router.txnBean().put(inner, null);
			if(flush){
				getSession(client.getName()).flush();
				getSession(client.getName()).clear();
				Assert.assertTrue(router.txnBean().exists(inner.getKey(), null));
				Assert.assertTrue(router.txnBean().exists(new TxnBeanKey("outer"), null));
			}else{
				boolean fieldAware = router.txnBean().getFieldInfo().getFieldAware();
				if(fieldAware || SessionExecutorImpl.EAGER_SESSION_FLUSH){
					Assert.assertTrue(router.txnBean().exists(inner.getKey(), null));
					Assert.assertTrue(router.txnBean().exists(new TxnBeanKey("outer"), null));
				}else{
					Assert.assertTrue(router.txnBean().exists(new TxnBeanKey("outer"), null));
					Assert.assertFalse(router.txnBean().exists(inner.getKey(), null));
				}
			}
			return null;
		}
		
	}
}