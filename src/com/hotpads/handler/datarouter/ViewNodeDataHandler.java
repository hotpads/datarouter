package com.hotpads.handler.datarouter;

import javax.inject.Inject;

import com.hotpads.datarouter.client.Clients;
import com.hotpads.datarouter.node.Nodes;
import com.hotpads.handler.BaseHandler;

public class ViewNodeDataHandler extends BaseHandler{	
	
	protected Clients clients;
	protected Nodes nodes;
	

	@Inject ViewNodeDataHandler(Clients clients, Nodes nodes){
		
	}
	
	
	/*************************** instance ****************************************/
	
//	@Override
//	protected Mav handleDefault(){
//		return summary();
//	}
		
	/***************************** handlers ********************************/
	
//	@Handler Mav summary(){
//		modelIndexTreeFactory.getInitializedTree();
//		return new InContextRedirectMav(params, "/admin");
//	}
	
	
}
