/**
 * 
 */
package com.hotpads.datarouter.test.client.txn;

import java.util.List;

import org.junit.Assert;

import com.hotpads.datarouter.app.client.parallel.jdbc.base.BaseParallelHibernateTxnApp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.client.BasicClientTestRouter;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class MultiInsertRollback extends BaseParallelHibernateTxnApp<Void>{
	BasicClientTestRouter router;
	boolean flush;
	public MultiInsertRollback(BasicClientTestRouter router, boolean flush){
		super(router);
		this.router = router;
		this.flush = flush;
	}
	@Override
	public List<String> getClientNames() {
		return ListTool.wrap(DRTestConstants.CLIENT_drTest0);
	}
	@Override
	public Void runOncePerClient(Client client){
		router.txnBean().putMulti(
				ListTool.create(
						new TxnBean("c"),
						new TxnBean("d"),
						new TxnBean("e")),
				null);
		if(flush){//tests calling this should already have 1 bean existing
			this.getSession(client.getName()).flush();
			Assert.assertEquals(4, CollectionTool.size(router.txnBean().getAll(null)));
		}else{
			Assert.assertEquals(1, CollectionTool.size(router.txnBean().getAll(null)));
		}
		throw new RuntimeException("belch");
	}
}