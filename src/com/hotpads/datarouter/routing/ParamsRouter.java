package com.hotpads.datarouter.routing;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hbase.HBaseClientImp;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientImp;
import com.hotpads.datarouter.client.imp.memory.MemoryClient;
import com.hotpads.datarouter.node.Node;
import com.hotpads.handler.Params;
import com.hotpads.handler.admin.RoutersHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.util.core.StringTool;

public class ParamsRouter<C extends Client> {

	private static final String PARAM_routerName = "routerName";
	private static final String PARAM_clientName = "clientName";
	private static final String PARAM_nodeName = "nodeName";
	private static final String PARAM_tableName = "tableName";
	public static final String NEEDS_CLIENT = "NEEDS_CLIENT";
	public static final String NEEDS_ROUTER = "NEEDS_ROUTER";
	public static final String NEEDS_NODE = "NEEDS_NODE";

	private String action;
	private HashMap<String, List<String>> needs;
	private String clientName;
	private String routerName;

	private String nodeName;
	private C client;
	private DataRouter router;
	private Node node;
	private String tableName;

	public ParamsRouter(DataRouterContext dataRouterContext, Params params,
			HashMap<String, List<String>> needs) {
		this.action = params.optional(RequestTool.SUBMIT_ACTION,
				RoutersHandler.ACTION_listRouters);
		this.needs = needs;
		initializeGlobalParameters(dataRouterContext, params);
	}

	public void initializeGlobalParameters(DataRouterContext dataRouterContext,
			Params params) {

		if (needs.get(NEEDS_ROUTER).contains(action)) {
			routerName = params.required(PARAM_routerName);
			router = dataRouterContext.getRouter(routerName);
		}
		if (needs.get(NEEDS_CLIENT).contains(action)) {
			clientName = params.required(PARAM_clientName);
			client = (C) dataRouterContext.getRouter(routerName).getClient(
					clientName);
			tableName = params.optional(PARAM_tableName, null);

		}
		if (needs.get(NEEDS_NODE).contains(action)) {
			nodeName = params.optional(PARAM_nodeName, null);
			tableName = params.required(PARAM_tableName);
			if (StringTool.notEmpty(nodeName)) {
				node = dataRouterContext.getRouter(routerName).getContext()
						.getNodes().getNode(nodeName);
			} else {
				setTableName(params.optional(PARAM_tableName, null));
				if (getTableName() != null) {
					node = dataRouterContext
							.getRouter(routerName)
							.getContext()
							.getNodes()
							.getPhyiscalNodeForClientAndTable(clientName,
									getTableName());
				}
			}
		}
	}

	public String getAction() {
		return action;
	}

	public HashMap<String, List<String>> getNeeds() {
		return needs;
	}

	public String getClientName() {
		return clientName;
	}

	public String getRouterName() {
		return routerName;
	}

	public String getNodeName() {
		return nodeName;
	}

	public C getClient() {
		return client;
	}

	public DataRouter getRouter() {
		return router;
	}

	public Node getNode() {
		return node;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setNeeds(HashMap<String, List<String>> needs) {
		this.needs = needs;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public void setRouterName(String routerName) {
		this.routerName = routerName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public void setClient(C client) {
		this.client = client;
	}

	public void setRouter(DataRouter router) {
		this.router = router;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
