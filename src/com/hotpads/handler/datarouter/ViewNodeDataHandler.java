package com.hotpads.handler.datarouter;

import com.google.inject.Inject;
import com.hotpads.datarouter.client.Clients;
import com.hotpads.datarouter.node.Nodes;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.InContextRedirectMav;

public class ViewNodeDataHandler extends BaseHandler{	
	
	protected Clients clients;
	protected Nodes nodes;
	

	@Inject ViewNodeDataHandler(Clients clients, Nodes nodes){
		
	}
	
	
	/*************************** instance ****************************************/
	
	@Override
	protected Mav handleDefault(){
		return summary();
	}
		
	/***************************** handlers ********************************/
	
	@Handler Mav summary(){
		modelIndexTreeFactory.getInitializedTree();
		return new InContextRedirectMav(params, "/admin");
	}
	
	
}
