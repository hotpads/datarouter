/**
 * 
 */
package com.hotpads.datarouter.test.client.txn.txnapp;

import java.util.List;

import org.junit.Assert;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.type.ConnectionClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.connection.ConnectionHandle;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.TxnBeanKey;
import com.hotpads.datarouter.test.client.txn.TxnTestRouter;

public class TestNestedTxn extends BaseJdbcOp<Void>{
	
	private Datarouter datarouter;
	private List<String> clientNames;
	private Isolation isolation;
	private TxnTestRouter router;
	
	public TestNestedTxn(Datarouter datarouter, List<String> clientNames, Isolation isolation, boolean autoCommit,
			TxnTestRouter router){
		super(datarouter, clientNames, isolation, autoCommit);
		this.datarouter = datarouter;
		this.clientNames = clientNames;
		this.isolation = isolation;
		this.router = router;
	}
	
	@Override
	public Void runOncePerClient(Client client){
		ConnectionClient connectionClient = (ConnectionClient)client;
		ConnectionHandle handle = connectionClient.getExistingHandle();
		
		TxnBean outer = new TxnBean("outer");
		router.txnBean().put(outer, null);
		Assert.assertTrue(router.txnBean().exists(outer.getKey(), null));
		
		datarouter.run(new InnerTxn(datarouter, clientNames, isolation, false, router, handle));
		
		TxnBean outer2 = new TxnBean(outer.getId());
		router.txnBean().put(outer2, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));//should bust on commit
		return null;
	}
	
	
	public static class InnerTxn extends BaseJdbcOp<Void>{
		private TxnTestRouter router;
		private ConnectionHandle outerHandle;

		public InnerTxn(Datarouter datarouter, List<String> clientNames, Isolation isolation, boolean autoCommit,
				TxnTestRouter router, ConnectionHandle outerHandle){
			super(datarouter, clientNames, isolation, autoCommit);
			this.router = router;
			this.outerHandle = outerHandle;
		}
		
		@Override
		public Void runOncePerClient(Client client){
			ConnectionClient connectionClient = (ConnectionClient)client;
			ConnectionHandle handle = connectionClient.getExistingHandle();
			Assert.assertEquals(outerHandle, handle);
			
			String name = "inner_txn";
			TxnBean inner = new TxnBean(name);
			router.txnBean().put(inner, null);
			Assert.assertTrue(router.txnBean().exists(inner.getKey(), null));
			Assert.assertTrue(router.txnBean().exists(new TxnBeanKey("outer"), null));
			return null;
		}
		
	}
}