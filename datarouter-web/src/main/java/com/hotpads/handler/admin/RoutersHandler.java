package com.hotpads.handler.admin;

import java.util.List;
import java.util.SortedSet;

import javax.inject.Inject;

import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.util.node.NodeWrapper;

public class RoutersHandler extends BaseHandler {

	/************************************** Constants **********************************/
	public static final String
		ACTION_listRouters = "listRouters",
		ACTION_inspectRouter = "inspectRouter",
		ACTION_inspectClient = "inspectClient";

	public static final String PARAM_routerName = "routerName";
	public static final String PARAM_clientName = "clientName";
	public static final String PARAM_nodeName = "nodeName";
	public static final String PARAM_tableName = "tableName";
	public static final String PARAM_columnName = "columnName";

	/******************* fields ********************/

	// injected
	@Inject
	private Datarouter datarouter;

	// not injected
	private Router router;
	private Mav mav;
	private String routerName = null;

	/************* Handler methods ********************/
	@Override
	@Handler
	protected Mav handleDefault() {
		mav = new Mav("/jsp/admin/datarouter/datarouterMenu.jsp");
		SortedSet<Router> routers = datarouter.getRouters();
//		initClients(routers);
		mav.put("serverName", datarouter.getServerName());
		mav.put("administratorEmail", datarouter.getAdministratorEmail());
		mav.put("routers", routers);
		return mav;
	}

	@Handler
	Mav inspectRouter() {
		routerName = params.required(PARAM_routerName);
		router = datarouter.getRouter(routerName);
		mav = new Mav("/jsp/admin/datarouter/routerSummary.jsp");
		List<NodeWrapper> nodeWrappers = NodeWrapper.getNodeWrappers(router);
		mav.put("nodeWrappers", nodeWrappers);
		return mav;
	}
}