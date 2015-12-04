package com.hotpads.trace.test;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.EntityNodeFactory;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.trace.node.TraceCompoundNode;
import com.hotpads.trace.node.TraceEntityNode;


@Singleton
public class TraceTestRouter extends BaseRouter{

	public static final String
			NAME = "TraceRouter",
			NODE_TraceEntity = "TraceEntity",
			NODE_TraceCompound = "TraceCompound";

	private final EntityNodeFactory entityNodeFactory;
	private final NodeFactory nodeFactory;
	private final Datarouter datarouter;

	@Inject
	public TraceTestRouter(Datarouter datarouter, EntityNodeFactory entityNodeFactory, NodeFactory nodeFactory){
		super(datarouter, DrTestConstants.CONFIG_PATH, NAME);
		this.datarouter = datarouter;
		this.entityNodeFactory = entityNodeFactory;
		this.nodeFactory = nodeFactory;
		createNodes();
	}

	/********************************** config **********************************/

	public static final List<ClientId> CLIENT_IDS = Arrays.asList(DrTestConstants.CLIENT_drTestHBase);

	@Override
	public List<ClientId> getClientIds(){
		return CLIENT_IDS;
	}

	/********************************** nodes **********************************/

	private TraceCompoundNode traceCompound;
	private TraceEntityNode traceEntity;


	private void createNodes(){
		traceEntity = new TraceEntityNode(entityNodeFactory, nodeFactory, this, DrTestConstants.CLIENT_drTestHBase,
				TraceEntityNode.ENTITY_NODE_PARAMS_TraceEntityTest);
		traceCompound = new TraceCompoundNode(nodeFactory, this, datarouter, DrTestConstants.CLIENT_drTestHBase,
				NODE_TraceCompound);
	}

	/*************************** get/set ***********************************/

	public TraceEntityNode traceEntity(){
		return traceEntity;
	}

	public TraceCompoundNode traceCompound(){
		return traceCompound;
	}

}
