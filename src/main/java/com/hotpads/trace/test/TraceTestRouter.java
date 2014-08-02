package com.hotpads.trace.test;
import java.util.List;

import com.google.inject.Singleton;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.trace.node.TraceCompoundNode;
import com.hotpads.trace.node.TraceEntityNode;
import com.hotpads.util.core.ListTool;


@Singleton
public class TraceTestRouter extends BaseDataRouter{

	public static final String 
			NAME = "TraceRouter",
			NODE_TraceEntity = "TraceEntity",
			NODE_TraceCompound = "TraceCompound";
	
	public TraceTestRouter(){
		super(new DataRouterContext(), NAME);
		initNodes();
		registerWithContext();//do after field inits
	}

	/********************************** config **********************************/
		
	@Override
	public String getConfigLocation(){
		return DRTestConstants.CONFIG_PATH;
	}

	public static final List<ClientId> CLIENT_IDS = ListTool.create(
			new ClientId(DRTestConstants.CLIENT_drTestHBase, true));
	
	@Override
	public List<ClientId> getClientIds(){
		return CLIENT_IDS;
	}


	
	/********************************** nodes **********************************/
	
	private TraceCompoundNode traceCompound;
	private TraceEntityNode traceEntity;
	
	
	private void initNodes(){
		traceEntity = new TraceEntityNode(this, DRTestConstants.CLIENT_drTestHBase, NODE_TraceEntity);
		traceCompound = new TraceCompoundNode(this, DRTestConstants.CLIENT_drTestHBase, NODE_TraceCompound);
	}
	
	/*************************** get/set ***********************************/

	public TraceEntityNode traceEntity(){
		return traceEntity;
	}
	
	public TraceCompoundNode traceCompound(){
		return traceCompound;
	}

}





