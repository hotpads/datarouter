/**
 * 
 */
package com.hotpads.datarouter.test.client.txn.txnapp;

import java.util.List;

import org.junit.Assert;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.client.BasicClientTestRouter;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.TxnTestRouter;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

public class MultiInsertRollback extends BaseHibernateOp<Void>{
	
	private TxnTestRouter router;
	private boolean flush;
	private String beanPrefix;
	
	public MultiInsertRollback(DatarouterContext drContext, List<String> clientNames, Isolation isolation,
			TxnTestRouter router, boolean flush, String beanPrefix){
		super(drContext, clientNames, isolation, false);
		this.router = router;
		this.flush = flush;
		this.beanPrefix = beanPrefix;
	}
	
	@Override
	public Void runOncePerClient(Client client){
		List<TxnBean> beans = ListTool.create(
				new TxnBean(beanPrefix + "2"),
				new TxnBean(beanPrefix + "3"));
		
		router.txnBean().putMulti(beans, null);
		
		if(flush){//tests calling this should already have 1 bean existing
			this.getSession(client.getName()).flush();
			Assert.assertTrue(router.txnBean().exists(beans.get(0).getKey(), null));
			Assert.assertTrue(router.txnBean().exists(beans.get(1).getKey(), null));
		}else{
			boolean fieldAware = router.txnBean().getFieldInfo().getFieldAware();
			if(fieldAware || SessionExecutorImpl.EAGER_SESSION_FLUSH){
				Assert.assertTrue(router.txnBean().exists(beans.get(0).getKey(), null));
				Assert.assertTrue(router.txnBean().exists(beans.get(1).getKey(), null));
			}else{
				Assert.assertFalse(router.txnBean().exists(beans.get(0).getKey(), null));
				Assert.assertFalse(router.txnBean().exists(beans.get(1).getKey(), null));
			}
		}
		throw new RuntimeException("belch");
	}
}