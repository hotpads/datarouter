/**
 * 
 */
package com.hotpads.datarouter.test.client.txn.txnapp;

import java.util.List;

import org.junit.Assert;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.TxnTestRouter;
import com.hotpads.datarouter.util.core.DrListTool;

public class TestMultiInsertRollback extends BaseJdbcOp<Void>{
	
	private TxnTestRouter router;
	private String beanPrefix;
	
	public TestMultiInsertRollback(Datarouter datarouter, List<String> clientNames, Isolation isolation,
			TxnTestRouter router, String beanPrefix){
		super(datarouter, clientNames, isolation, false);
		this.router = router;
		this.beanPrefix = beanPrefix;
	}
	
	@Override
	public Void runOncePerClient(Client client){
		List<TxnBean> beans = DrListTool.create(
				new TxnBean(beanPrefix + "2"),
				new TxnBean(beanPrefix + "3"));
		
		router.txnBean().putMulti(beans, null);
		Assert.assertTrue(router.txnBean().exists(beans.get(0).getKey(), null));
		Assert.assertTrue(router.txnBean().exists(beans.get(1).getKey(), null));
		throw new RuntimeException("belch");
	}
}