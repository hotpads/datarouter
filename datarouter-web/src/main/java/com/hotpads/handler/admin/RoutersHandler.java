package com.hotpads.handler.admin;

import java.util.List;

import javax.inject.Inject;

import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.InContextRedirectMav;
import com.hotpads.handler.util.node.NodeWrapper;

public class RoutersHandler extends BaseHandler {

	public static final String
		ACTION_listRouters = "listRouters",
		ACTION_inspectRouter = "inspectRouter",
		ACTION_inspectClient = "inspectClient";

	public static final String
		PARAM_routerName = "routerName",
		PARAM_clientName = "clientName",
		PARAM_nodeName = "nodeName",
		PARAM_tableName = "tableName",
		PARAM_columnName = "columnName";

	public static final String
		JSP_datarouterMenu = "/jsp/admin/datarouter/datarouterMenu.jsp",
		JSP_routerSummary = "/jsp/admin/datarouter/routerSummary.jsp";


	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterClients datarouterClients;


	@Override
	@Handler
	protected Mav handleDefault(){
		Mav mav = new Mav(JSP_datarouterMenu);
		mav.put("serverName", datarouterProperties.getServerName());
		mav.put("administratorEmail", datarouterProperties.getAdministratorEmail());
		mav.put("routers", datarouter.getRouters());
		mav.put("lazyClientProviderByName", datarouterClients.getLazyClientProviderByName());
		mav.put("uninitializedClientNames", datarouterClients.getClientNamesByInitialized().get(false));
		return mav;
	}

	@Handler
	private Mav initClient(String clientName){
		datarouterClients.getClient(clientName);
		return new InContextRedirectMav(params, DatarouterWebDispatcher.PATH_routers);
	}

	@Handler
	private Mav initAllClients(){
		datarouterClients.getAllClients();
		return new InContextRedirectMav(params, DatarouterWebDispatcher.PATH_routers);
	}

	@Handler
	private Mav inspectRouter(){
		Mav mav = new Mav(JSP_routerSummary);
		String routerName = params.required(PARAM_routerName);
		Router router = datarouter.getRouter(routerName);
		List<NodeWrapper> nodeWrappers = NodeWrapper.getNodeWrappers(router);
		mav.put("nodeWrappers", nodeWrappers);
		return mav;
	}

}
