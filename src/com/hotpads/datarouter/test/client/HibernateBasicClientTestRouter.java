package com.hotpads.datarouter.test.client;
import java.io.IOException;

import com.google.inject.Singleton;
import com.hotpads.datarouter.node.HibernateNodeFactory;
import com.hotpads.datarouter.node.op.MapStorageNode;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.TxnBeanKey;


@Singleton
public class HibernateBasicClientTestRouter
extends BaseDataRouter implements BasicClientTestRouter{

	public static final String name = "basicClientTest";
	
	public HibernateBasicClientTestRouter() throws IOException{
		super(name);
		activate();//do after field inits
	}

	/********************************** config **********************************/
	
	@Override
	public String getConfigLocation(){
		return DRTestConstants.CONFIG_PATH;
	}

	/********************************** client names **********************************/
	
	public static final String 
		client_drTest0 = DRTestConstants.CLIENT_drTestHibernate0;

	
	/********************************** nodes **********************************/
	
	private MapStorageNode<TxnBeanKey,TxnBean> txnBean = register(
			HibernateNodeFactory.newHibernate(client_drTest0, TxnBean.class, this));
	
	
	
	
	/*************************** get/set ***********************************/


	public MapStorageNode<TxnBeanKey,TxnBean> txnBean() {
		return txnBean;
	}

}





