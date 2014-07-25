package com.hotpads.handler.admin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.routing.comparator.ComparatorDataRouter;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.util.node.NodeWrapper;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

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
	private DataRouterContext dataRouterContext;

	// not injected
	private DataRouter router;
	private Mav mav;
	private String routerName = null;

	/************* Handler methods ********************/
	@Handler
	protected Mav handleDefault() {
		mav = new Mav("/jsp/admin/datarouter/dataRouterMenu.jsp");
		List<DataRouter> routers = dataRouterContext.getRouters();
//		initClients(routers);
		mav.put("serverName", dataRouterContext.getServerName());
		mav.put("administratorEmail", dataRouterContext.getAdministratorEmail());
		Collections.sort(routers, new ComparatorDataRouter());
		mav.put("routers", routers);
		return mav;
	}

	@Handler
	Mav inspectRouter() {
		routerName = params.required(PARAM_routerName);
		router = dataRouterContext.getRouter(routerName);
		mav = new Mav("/jsp/admin/datarouter/routerSummary.jsp");
		List<NodeWrapper> nodeWrappers = NodeWrapper.getNodeWrappers(router);
		mav.put("nodeWrappers", nodeWrappers);
		return mav;

	}

//	public static void initClients(Collection<DataRouter> routers) {
//		List<String> allClientNames = ListTool.create();
//		for (final DataRouter router : IterableTool.nullSafe(routers)) {
//			allClientNames.addAll(CollectionTool.nullSafe(router
//					.getClientNames()));
//		}
//		ExecutorService exec = Executors.newFixedThreadPool(CollectionTool
//				.size(allClientNames));
//		for (final DataRouter router : IterableTool.nullSafe(routers)) {
//			for (final String clientName : IterableTool.nullSafe(router
//					.getClientNames())) {
//				exec.submit(new Callable<Void>() {
//					public Void call() {
//						router.getClient(clientName);
//						return null;
//					}
//				});
//			}
//		}
//		exec.shutdown();
//	}

	
}
