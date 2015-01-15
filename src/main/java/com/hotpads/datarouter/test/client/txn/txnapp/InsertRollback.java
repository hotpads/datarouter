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
import com.hotpads.datarouter.test.client.BasicClientTestRouter;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.util.core.IterableTool;

public class InsertRollback extends BaseHibernateOp<Void>{
	
	private BasicClientTestRouter router;
	private boolean flush;
	
	public InsertRollback(DatarouterContext drContext, List<String> clientNames, Isolation isolation,
			BasicClientTestRouter router, boolean flush){
		super(drContext, clientNames, isolation, false);
		this.router = router;
		this.flush = flush;
	}
	
	@Override
	public Void runOncePerClient(Client client){
		TxnBean a = new TxnBean("a");
		router.txnBeanHibernate().put(a, null);
		if(flush){
			Session session = getSession(client.getName());
			session.flush();
			session.clear();
			Assert.assertEquals(1, IterableTool.count(router.txnBeanHibernate().scan(null, null)).intValue());
		}else{
			boolean fieldAware = router.txnBeanHibernate().getFieldInfo().getFieldAware();
			if(fieldAware || SessionExecutorImpl.EAGER_SESSION_FLUSH){
				Assert.assertEquals(1, IterableTool.count(router.txnBeanHibernate().scan(null, null)).intValue());
			}else{
				Assert.assertEquals(0, IterableTool.count(router.txnBeanHibernate().scan(null, null)).intValue());
			}
		}
		TxnBean a2 = new TxnBean("a2");
		a2.setId(a.getId());
		router.txnBeanHibernate().put(a2, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));//should bust on commit
		return null;
	}
}