package com.hotpads.trace.test;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.inject.Singleton;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.routing.BaseDatarouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.util.core.ListTool;
import com.hotpads.trace.node.TraceCompoundNode;
import com.hotpads.trace.node.TraceEntityNode;


@Singleton
public class TraceTestRouter extends BaseDatarouter{

	public static final String 
			NAME = "TraceRouter",
			NODE_TraceEntity = "TraceEntity",
			NODE_TraceCompound = "TraceCompound";

	private final NodeFactory nodeFactory;
	
	@Inject
	public TraceTestRouter(DatarouterContext drContext, NodeFactory nodeFactory){
		super(drContext, DRTestConstants.CONFIG_PATH, NAME);
		this.nodeFactory = nodeFactory;
		createNodes();
		registerWithContext();//do after field inits
	}

	/********************************** config **********************************/

	public static final List<ClientId> CLIENT_IDS = ListTool.create(
			new ClientId(DRTestConstants.CLIENT_drTestHBase, true));
	
	@Override
	public List<ClientId> getClientIds(){
		return CLIENT_IDS;
	}


	
	/********************************** nodes **********************************/
	
	private TraceCompoundNode traceCompound;
	private TraceEntityNode traceEntity;
	
	
	private void createNodes(){
		traceEntity = new TraceEntityNode(nodeFactory, this, DRTestConstants.CLIENT_drTestHBase, 
				TraceEntityNode.ENTITY_NODE_PARAMS_TraceEntityTest);
		traceCompound = new TraceCompoundNode(nodeFactory, this, DRTestConstants.CLIENT_drTestHBase, NODE_TraceCompound);
	}
	
	/*************************** get/set ***********************************/

	public TraceEntityNode traceEntity(){
		return traceEntity;
	}
	
	public TraceCompoundNode traceCompound(){
		return traceCompound;
	}

}





