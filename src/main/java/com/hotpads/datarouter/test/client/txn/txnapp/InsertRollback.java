/**
 * 
 */
package com.hotpads.datarouter.test.client.txn.txnapp;

import java.util.List;

import org.hibernate.Session;
import org.junit.Assert;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.TxnTestRouter;

public class InsertRollback extends BaseHibernateOp<Void>{
	
	private TxnTestRouter router;
	private boolean flush;
	private String beanPrefix;
	
	public InsertRollback(DatarouterContext drContext, List<String> clientNames, Isolation isolation,
			TxnTestRouter router, boolean flush, String beanPrefix){
		super(drContext, clientNames, isolation, false);
		this.router = router;
		this.flush = flush;
		this.beanPrefix = beanPrefix;
	}
	
	@Override
	public Void runOncePerClient(Client client){
		TxnBean a = new TxnBean(beanPrefix + "1");
		router.txnBean().put(a, null);
		if(flush){
			Session session = getSession(client.getName());
			session.flush();
			session.clear();
			Assert.assertTrue(router.txnBean().exists(a.getKey(), null));//it exists inside the txn
		}else{
			boolean fieldAware = router.txnBean().getFieldInfo().getFieldAware();
			if(fieldAware || SessionExecutorImpl.EAGER_SESSION_FLUSH){
				Assert.assertTrue(router.txnBean().exists(a.getKey(), null));
			}else{
				Assert.assertFalse(router.txnBean().exists(a.getKey(), null));
			}
		}
		TxnBean a2 = new TxnBean(beanPrefix + "2");
		a2.setId(a.getId());
		router.txnBean().put(a2, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));//should bust on commit
		return null;
	}
}