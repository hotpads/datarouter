/**
 * 
 */
package com.hotpads.datarouter.test.client.txn.txnapp;

import java.util.List;

import org.junit.Assert;

import com.hotpads.datarouter.app.client.parallel.jdbc.base.BaseParallelHibernateTxnApp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.HibernateExecutor;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.client.BasicClientTestRouter;
import com.hotpads.datarouter.test.client.txn.TxnBean;
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
		return ListTool.wrap(DRTestConstants.CLIENT_drTestHibernate0);
	}
	@Override
	public Void runOncePerClient(Client client){
		List<TxnBean> beans = ListTool.create(
				new TxnBean("c"),
				new TxnBean("d"),
				new TxnBean("e"));
		
		router.txnBean().putMulti(beans, null);
		
		if(flush){//tests calling this should already have 1 bean existing
			this.getSession(client.getName()).flush();
			Assert.assertEquals(4, CollectionTool.size(router.txnBean().getAll(null)));
		}else{
			List<TxnBean> all = router.txnBean().getAll(null);
			if(CollectionTool.getFirst(beans).isFieldAware() || HibernateExecutor.EAGER_SESSION_FLUSH){
				Assert.assertEquals(4, CollectionTool.size(all));
			}else{
				Assert.assertEquals(1, CollectionTool.size(all));
			}
		}
		throw new RuntimeException("belch");
	}
}