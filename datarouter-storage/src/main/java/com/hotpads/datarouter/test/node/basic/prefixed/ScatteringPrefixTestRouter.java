package com.hotpads.datarouter.test.node.basic.prefixed;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.node.op.raw.SortedStorage;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.prefixed.ScatteringPrefixBean.ScatteringPrefixBeanFielder;

@Singleton
public class ScatteringPrefixTestRouter extends BaseRouter{

	@Inject
	public ScatteringPrefixTestRouter(Datarouter datarouter, DatarouterSettings datarouterSettings,
			NodeFactory nodeFactory, ClientId clientId){
		super(datarouter, DrTestConstants.CONFIG_PATH, ScatteringPrefixTestRouter.class.getSimpleName(), nodeFactory,
				datarouterSettings);

		scatteringPrefixBeanNode = register(nodeFactory.create(clientId, ScatteringPrefixBean.class,
				ScatteringPrefixBeanFielder.class, this, false));

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