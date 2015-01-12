package com.hotpads.datarouter.test.client.reconnect;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.test.DatarouterTestInjectorProvider;
import com.hotpads.datarouter.test.client.BasicClientTestRouter;
import com.hotpads.datarouter.test.client.BasicClientTestRouterImp;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.TxnBeanKey;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.profile.PhaseTimer;

//you must run this manually, starting and stopping hbase to verify it reconnects, at least for now
public class HBaseClientReconnectTester {
	Logger logger = LoggerFactory.getLogger(HBaseClientReconnectTester.class);
	
	//DoNotCommit//will loop forever in the test suite
//	static boolean ENABLED = true;
	static boolean ENABLED = false;
	
	static BasicClientTestRouter router;
	static TxnBeanKey testReconnectBeanKey = new TxnBeanKey("testReconnectBean");
	static SortedMapStorage<TxnBeanKey,TxnBean> node;

	public static void resetTable(){
//		node.deleteAll(null);
//		Assert.assertEquals(0, CollectionTool.size(node.getAll(null)));
		TxnBean txnBean = new TxnBean(testReconnectBeanKey.getId());
		node.put(txnBean, null);
		Assert.assertEquals(1, IterableTool.count(node.scan(null, null)).intValue());
	}
	
	@BeforeClass
	public static void init() throws IOException{
		Injector injector = new DatarouterTestInjectorProvider().get();
		router = injector.getInstance(BasicClientTestRouterImp.class);	
		node = router.txnBeanHBase();
		resetTable();
	}

	@Test 
	public void testReconnect(){
		if(!ENABLED){ return; }
		int periodMs = 5000;
		while(true){
			Config config = new Config()
					.setTimeoutMs((long)periodMs)
					.setNumAttempts(1);
			try{
				PhaseTimer timer = new PhaseTimer();
				TxnBean gotBean = node.get(testReconnectBeanKey, config);
				Assert.assertNotNull(gotBean);
				timer.add("got bean");
				logger.warn(timer.toString());
				Thread.sleep(periodMs);
			}catch(DataAccessException dae){
				logger.warn("", dae);
			}catch(InterruptedException ie){
				throw new RuntimeException("who's interrupting me?", ie);
			}
		}
	}
	
}
