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
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.DatarouterTestInjectorProvider;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.TxnBeanKey;
import com.hotpads.datarouter.test.client.txn.TxnTestRouter;
import com.hotpads.datarouter.util.core.IterableTool;
import com.hotpads.util.core.profile.PhaseTimer;

//you must run this manually, starting and stopping hbase to verify it reconnects, at least for now
public class HBaseClientReconnectTester {
	private static final Logger logger = LoggerFactory.getLogger(HBaseClientReconnectTester.class);
	
	//DoNotCommit//will loop forever in the test suite
//	static boolean ENABLED = true;
	private static boolean ENABLED = false;
	
	private static DatarouterContext datarouterContext;
	private static TxnTestRouter router;
	private static TxnBeanKey testReconnectBeanKey = new TxnBeanKey("testReconnectBean");
	private static SortedMapStorage<TxnBeanKey,TxnBean> node;
	
	@BeforeClass
	public static void beforeClass() throws IOException{
		Injector injector = new DatarouterTestInjectorProvider().get();
		datarouterContext = injector.getInstance(DatarouterContext.class);
		NodeFactory nodeFactory = injector.getInstance(NodeFactory.class);
		router = new TxnTestRouter(datarouterContext, nodeFactory, DRTestConstants.CLIENT_drTestHBase, true);
		node = router.txnBean();
		resetTable();
	}

	public static void resetTable(){
//		node.deleteAll(null);
//		Assert.assertEquals(0, CollectionTool.size(node.getAll(null)));
		TxnBean txnBean = new TxnBean(testReconnectBeanKey.getId());
		node.put(txnBean, null);
		Assert.assertEquals(1, IterableTool.count(node.scan(null, null)).intValue());
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
