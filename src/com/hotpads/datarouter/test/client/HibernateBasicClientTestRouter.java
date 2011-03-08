package com.hotpads.datarouter.test.client;
import java.io.IOException;
import java.util.List;

import com.google.inject.Singleton;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.TxnBeanKey;
import com.hotpads.util.core.ListTool;


@Singleton
public class HibernateBasicClientTestRouter
extends BaseDataRouter 
implements BasicClientTestRouter{

	public static final String name = "basicClientTest";
	
	public HibernateBasicClientTestRouter() throws IOException{
		super(null, name, CLIENT_IDS);
		activate();//do after field inits
	}

	/********************************** config **********************************/
	
	@Override
	public String getConfigLocation(){
		return DRTestConstants.CONFIG_PATH;
	}

	/********************************** client names **********************************/
	
	public static final String 
		CLIENT_drTest0 = DRTestConstants.CLIENT_drTestHibernate0;

	public static final List<ClientId>
		CLIENT_IDS = ListTool.create(new ClientId(CLIENT_drTest0, true));
	
	
	/********************************** nodes **********************************/
	
	private MapStorage<TxnBeanKey,TxnBean> txnBean = cast(register(
			NodeFactory.create(CLIENT_drTest0, TxnBean.class, this)));
	
	
	
	
	/*************************** get/set ***********************************/


	public MapStorage<TxnBeanKey,TxnBean> txnBean() {
		return txnBean;
	}

}





