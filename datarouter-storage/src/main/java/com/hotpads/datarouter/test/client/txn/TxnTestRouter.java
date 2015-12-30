package com.hotpads.datarouter.test.client.txn;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.client.txn.TxnBean.TxnBeanFielder;

@Singleton
public class TxnTestRouter extends BaseRouter{

	private final List<ClientId> clientIds;

	/********************************** nodes **********************************/

	private SortedMapStorageNode<TxnBeanKey,TxnBean> txnBean;


	/********************************* constructor *****************************/

	@Inject
	public TxnTestRouter(Datarouter datarouter, NodeFactory nodeFactory, ClientId clientId, boolean useFielder){
		super(datarouter, DrTestConstants.CONFIG_PATH, TxnTestRouter.class.getSimpleName()+"Router");

		this.clientIds = new ArrayList<>();
		this.clientIds.add(clientId);

		Class<TxnBeanFielder> fielderClass = useFielder ? TxnBeanFielder.class : null;
		txnBean = register(nodeFactory.create(clientId, TxnBean.class, fielderClass, this, false));

	}

	/********************************** config **********************************/

	@Override
	public List<ClientId> getClientIds(){
		return clientIds;
	}


	/*************************** get/set ***********************************/


	public SortedMapStorageNode<TxnBeanKey,TxnBean> txnBean(){
		return txnBean;
	}

}