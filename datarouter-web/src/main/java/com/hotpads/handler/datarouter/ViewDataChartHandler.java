package com.hotpads.handler.datarouter;

import javax.inject.Inject;

import com.hotpads.datarouter.node.DatarouterNodes;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.util.http.RequestTool;

public class ViewDataChartHandler<
PK extends PrimaryKey<PK>,
D extends Databean<PK,D>,
F extends DatabeanFielder<PK,D>>
extends BaseHandler{

	public static final String
		PARAM_routerName = "routerName",
		PARAM_nodeName = "nodeName";

	private final DatarouterNodes nodes;
	private Node<?,?> node;



	@Inject
	public ViewDataChartHandler(DatarouterNodes nodes){
		this.nodes = nodes;
	}


	private Mav preHandle(){
		Mav mav = new Mav("/jsp/admin/viewDataChart.jsp");
		String nodeName = RequestTool.get(request, PARAM_nodeName);
		node = nodes.getNode(nodeName);
		if(node == null){ return new MessageMav("Cannot find node " + nodeName); }
		mav.put("node", node);

	//	limit = RequestTool.getIntegerAndPut(request, PARAM_limit, 100, mav);
		return mav;
	}

	@Override
	protected Mav handleDefault(){
		return preHandle();
	}

	@Handler
	protected Mav viewDataChart(){
		Mav mav = preHandle();

		String nodeName = RequestTool.get(request, PARAM_nodeName);
		node = nodes.getNode(nodeName);
		System.out.println("node "+node.getName());


		return mav;

	}


}
