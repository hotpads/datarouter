package com.hotpads.datarouter.test.client.txn;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseDatarouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.client.txn.TxnBean.TxnBeanFielder;

@Singleton
public class TxnTestRouter
extends BaseDatarouter
{

	private String clientName;
	
	/********************************** nodes **********************************/

	private SortedMapStorageNode<TxnBeanKey,TxnBean> txnBean;

	
	/********************************* constructor *****************************/

	@Inject
	public TxnTestRouter(DatarouterContext drContext, NodeFactory nodeFactory, String clientName, boolean useFielder){
		super(drContext, DRTestConstants.CONFIG_PATH, TxnTestRouter.class.getSimpleName()+"Router");
		this.clientName = clientName;

		Class<TxnBeanFielder> fielderClass = useFielder ? TxnBeanFielder.class : null;
		txnBean = cast(register(nodeFactory.create(clientName, TxnBean.class, fielderClass, this, false)));
		
		registerWithContext();//do after field inits
	}

	/********************************** config **********************************/

	@Override
	public List<ClientId> getClientIds(){
		return Collections.singletonList(new ClientId(clientName, true));
	}


	/*************************** get/set ***********************************/


	public SortedMapStorageNode<TxnBeanKey,TxnBean> txnBean(){
		return txnBean;
	}

}





