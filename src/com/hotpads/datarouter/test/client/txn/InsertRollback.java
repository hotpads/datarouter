/**
 * 
 */
package com.hotpads.datarouter.test.client.txn;

import java.util.List;

import org.junit.Assert;

import com.hotpads.datarouter.app.client.parallel.jdbc.base.BaseParallelHibernateTxnApp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.client.BasicClientTestRouter;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class InsertRollback extends BaseParallelHibernateTxnApp<Void>{
	BasicClientTestRouter router;
	boolean flush;
	public InsertRollback(BasicClientTestRouter router, boolean flush){
		super(router);
		this.router = router;
		this.flush = flush;
	}
	@Override
	public List<String> getClientNames() {
		return ListTool.wrap(DRTestConstants.CLIENT_drTestHibernate0);
	}
	@Override
	public Void runOncePerClient(Client client){
		TxnBean a = new TxnBean("a");
		router.txnBean().put(a, null);
		if(flush){
			this.getSession(client.getName()).flush();
			this.getSession(client.getName()).clear();
			Assert.assertEquals(1, CollectionTool.size(router.txnBean().getAll(null)));
		}else{
			Assert.assertEquals(0, CollectionTool.size(router.txnBean().getAll(null)));
		}
		TxnBean a2 = new TxnBean("a2");
		a2.setId(a.getId());
		router.txnBean().put(a2, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));//should bust on commit
		return null;
	}
}