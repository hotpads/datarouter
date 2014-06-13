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
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.test.client.BasicClientTestRouter;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

public class MultiInsertRollback extends BaseHibernateOp<Void>{
	
	private BasicClientTestRouter router;
	private boolean flush;
	
	public MultiInsertRollback(DataRouterContext drContext, List<String> clientNames, Isolation isolation,
			BasicClientTestRouter router, boolean flush){
		super(drContext, clientNames, isolation, false);
		this.router = router;
		this.flush = flush;
	}
	
	@Override
	public Void runOncePerClient(Client client){
		List<TxnBean> beans = ListTool.create(
				new TxnBean("c"),
				new TxnBean("d"),
				new TxnBean("e"));
		
		router.txnBeanHibernate().putMulti(beans, null);
		
		if(flush){//tests calling this should already have 1 bean existing
			this.getSession(client.getName()).flush();
			Assert.assertEquals(4, IterableTool.count(router.txnBeanHibernate().scan(null, null)).intValue());
		}else{
			List<TxnBean> all = ListTool.createArrayList(router.txnBeanHibernate().scan(null, null));
			boolean fieldAware = router.txnBeanHibernate().getFieldInfo().getFieldAware();
			if(fieldAware || SessionExecutorImpl.EAGER_SESSION_FLUSH){
				Assert.assertEquals(4, CollectionTool.size(all));
			}else{
				Assert.assertEquals(1, CollectionTool.size(all));
			}
		}
		throw new RuntimeException("belch");
	}
}