package com.hotpads.handler.admin;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.routing.comparator.DatarouterComparator;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.util.node.NodeWrapper;

public class RoutersHandler extends BaseHandler {

	/************************************** Constants **********************************/
	private static Logger logger = LoggerFactory.getLogger(RoutersHandler.class);

	public static final String ACTION_listRouters = "listRouters",
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
	private DatarouterContext datarouterContext;

	// not injected
	private Datarouter router;
	private Mav mav;
	private String routerName = null;

	/************* Handler methods ********************/
	@Handler
	protected Mav handleDefault() {
		mav = new Mav("/jsp/admin/datarouter/datarouterMenu.jsp");
		List<Datarouter> routers = datarouterContext.getRouters();
//		initClients(routers);
		mav.put("serverName", datarouterContext.getServerName());
		mav.put("administratorEmail", datarouterContext.getAdministratorEmail());
		Collections.sort(routers, new DatarouterComparator());
		mav.put("routers", routers);
		return mav;
	}

	@Handler
	Mav inspectRouter() {
		routerName = params.required(PARAM_routerName);
		router = datarouterContext.getRouter(routerName);
		mav = new Mav("/jsp/admin/datarouter/routerSummary.jsp");
		List<NodeWrapper> nodeWrappers = NodeWrapper.getNodeWrappers(router);
		mav.put("nodeWrappers", nodeWrappers);
		return mav;

	}

}
