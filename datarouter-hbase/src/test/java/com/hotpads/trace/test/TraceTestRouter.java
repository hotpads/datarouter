package com.hotpads.trace.test;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.EntityNodeFactory;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.trace.node.TraceEntityNode;


@Singleton
public class TraceTestRouter extends BaseRouter{

	private static final String NAME = "TraceRouter";


	/********************************** nodes **********************************/

	private final TraceEntityNode traceEntity;

	@Inject
	public TraceTestRouter(Datarouter datarouter, DatarouterSettings datarouterSettings,
			EntityNodeFactory entityNodeFactory, NodeFactory nodeFactory){
		super(datarouter, DrTestConstants.CONFIG_PATH, NAME, nodeFactory, datarouterSettings);

		traceEntity = new TraceEntityNode(entityNodeFactory, nodeFactory, this, DrTestConstants.CLIENT_drTestHBase,
				TraceEntityNode.ENTITY_NODE_PARAMS_TraceEntityTest);
	}

	/*************************** get/set ***********************************/

	public TraceEntityNode traceEntity(){
		return traceEntity;
	}

}
