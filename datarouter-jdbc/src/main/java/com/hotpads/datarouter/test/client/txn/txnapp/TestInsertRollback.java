package com.hotpads.datarouter.test.client.txn.txnapp;

import java.util.List;

import org.junit.Assert;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.TxnTestRouter;

public class TestInsertRollback extends BaseJdbcOp<Void>{
	
	private TxnTestRouter router;
	private String beanPrefix;
	
	public TestInsertRollback(Datarouter datarouter, List<String> clientNames, Isolation isolation,
			TxnTestRouter router, String beanPrefix){
		super(datarouter, clientNames, isolation, false);
		this.router = router;
		this.beanPrefix = beanPrefix;
	}
	
	@Override
	public Void runOncePerClient(Client client){
		TxnBean a = new TxnBean(beanPrefix + "1");
		router.txnBean().put(a, null);
		Assert.assertTrue(router.txnBean().exists(a.getKey(), null));//it exists inside the txn
		TxnBean a2 = new TxnBean(beanPrefix + "2");
		a2.setId(a.getId());
		router.txnBean().put(a2, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));//should bust on commit
		return null;
	}
}