package com.hotpads.datarouter.test.client;
import java.io.IOException;
import java.util.List;

import com.google.inject.Singleton;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.connection.keepalive.KeepAlive;
import com.hotpads.datarouter.connection.keepalive.KeepAlive.KeepAliveFielder;
import com.hotpads.datarouter.connection.keepalive.KeepAliveKey;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.TxnBean.TxnBeanFielder;
import com.hotpads.datarouter.test.client.txn.TxnBeanKey;
import com.hotpads.util.core.ListTool;


@Singleton
public class BasicClientTestRouterImp
extends BaseDataRouter 
implements BasicClientTestRouter{
	
	public static final String name = "basicClientTest";	

	public BasicClientTestRouterImp() throws IOException{
		super(null, name, CLIENT_IDS);
		keepAliveHBase = cast(register(
				NodeFactory.create(DRTestConstants.CLIENT_drTestHBase, KeepAlive.class, KeepAliveFielder.class, this)));
		
		txnBeanHibernate = cast(register(
				NodeFactory.create(DRTestConstants.CLIENT_drTestHibernate0, TxnBean.class, TxnBeanFielder.class, this)));
		txnBeanHBase = cast(register(
				NodeFactory.create(DRTestConstants.CLIENT_drTestHBase, TxnBean.class, TxnBeanFielder.class, this)));
		
		activate();//do after field inits
	}

	/********************************** config **********************************/
	
	@Override
	public String getConfigLocation(){
		return DRTestConstants.CONFIG_PATH;
	}
	
	public static final List<ClientId> CLIENT_IDS = ListTool.create(
			new ClientId(DRTestConstants.CLIENT_drTestHibernate0, true),
			new ClientId(DRTestConstants.CLIENT_drTestHBase, true));
	
	/********************************** nodes **********************************/

	private MapStorage<KeepAliveKey,KeepAlive> keepAliveHBase;
	
	private MapStorage<TxnBeanKey,TxnBean> txnBeanHibernate;
	private MapStorage<TxnBeanKey,TxnBean> txnBeanHBase;
	
	
	/*************************** get/set ***********************************/


	public MapStorage<TxnBeanKey,TxnBean> txnBeanHibernate() {
		return txnBeanHibernate;
	}
	

	public MapStorage<TxnBeanKey,TxnBean> txnBeanHBase(){
		return txnBeanHBase;
	}

}





