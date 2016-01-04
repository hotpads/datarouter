package com.hotpads.datarouter.test.node.basic.prefixed;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.node.op.raw.SortedStorage;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.prefixed.ScatteringPrefixBean.ScatteringPrefixBeanFielder;

@Singleton
public class ScatteringPrefixTestRouter extends BaseRouter{

	private final List<ClientId> clientIds;

	@Inject
	public ScatteringPrefixTestRouter(Datarouter datarouter, NodeFactory nodeFactory, ClientId clientId){
		super(datarouter, DrTestConstants.CONFIG_PATH, ScatteringPrefixTestRouter.class.getSimpleName());

		this.clientIds = new ArrayList<>();
		this.clientIds.add(clientId);

		scatteringPrefixBeanNode = register(nodeFactory.create(clientId, ScatteringPrefixBean.class,
				ScatteringPrefixBeanFielder.class, this, false));

	}

	/********************************** config **********************************/

	@Override
	public List<ClientId> getClientIds(){
		return clientIds;
	}


	/********************************** nodes **********************************/

	private SortedMapStorage<ScatteringPrefixBeanKey,ScatteringPrefixBean> scatteringPrefixBeanNode;


	/*************************** get/set ***********************************/

	public SortedMapStorage<ScatteringPrefixBeanKey,ScatteringPrefixBean> scatteringPrefixBean(){
		return scatteringPrefixBeanNode;
	}

	public SortedStorage<ScatteringPrefixBeanKey,ScatteringPrefixBean> scatteringPrefixBeanSorted(){
		return scatteringPrefixBeanNode;
	}
}