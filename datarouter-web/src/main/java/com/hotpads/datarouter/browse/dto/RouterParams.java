package com.hotpads.datarouter.browse.dto;

import java.util.HashMap;
import java.util.List;

import com.hotpads.datarouter.browse.RoutersHandler;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.node.DatarouterNodes;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.params.Params;
import com.hotpads.util.http.RequestTool;

public class RouterParams<C extends Client>{

	public static final String NEEDS_CLIENT = "NEEDS_CLIENT";
	public static final String NEEDS_ROUTER = "NEEDS_ROUTER";
	public static final String NEEDS_NODE = "NEEDS_NODE";
	private static final String PARAM_routerName = "routerName";
	private static final String PARAM_clientName = "clientName";
	private static final String PARAM_nodeName = "nodeName";
	private static final String PARAM_tableName = "tableName";
	private String action;
	private HashMap<String, List<String>> needs;
	private String clientName;
	private String routerName;

	private String nodeName;
	private C client;
	private Router router;
	private Node<?, ?> node;
	private String tableName;

	public RouterParams(Datarouter datarouter, Params params, HashMap<String, List<String>> needs){
		this.action = params.optional(RequestTool.SUBMIT_ACTION, RoutersHandler.ACTION_listRouters);
		this.needs = needs;
		initializeGlobalParameters(datarouter, params);
	}

	public void initializeGlobalParameters(Datarouter datarouter, Params params){

		if(DrCollectionTool.nullSafe(needs.get(NEEDS_ROUTER)).contains(action)){
			routerName = params.required(PARAM_routerName);
			router = datarouter.getRouter(routerName);
		}
		if(DrCollectionTool.nullSafe(needs.get(NEEDS_CLIENT)).contains(action)){
			clientName = params.required(PARAM_clientName);
			client = (C)datarouter.getRouter(routerName).getClient(clientName);
			tableName = params.optional(PARAM_tableName, null);

		}
		if(DrCollectionTool.nullSafe(needs.get(NEEDS_NODE)).contains(action)){
			nodeName = params.optional(PARAM_nodeName, null);
			tableName = params.required(PARAM_tableName);
			if(DrStringTool.notEmpty(nodeName)){
				node = datarouter.getRouter(routerName).getContext().getNodes().getNode(nodeName);
			}else{
				setTableName(params.optional(PARAM_tableName, null));
				if(getTableName() != null){
					node = datarouter.getRouter(routerName).getContext().getNodes()
							.getPhyiscalNodeForClientAndTable(clientName, getTableName());
				}
			}
		}
	}

	public String getTableName(){
		return tableName;
	}

	public void setTableName(String tableName){
		this.tableName = tableName;
	}

	public DatarouterNodes getNodes(){
		return this.getContext().getNodes();
	}

	public Datarouter getContext(){
		return this.router.getContext();
	}

	public String getAction(){
		return action;
	}

	public void setAction(String action){
		this.action = action;
	}

	public HashMap<String, List<String>> getNeeds(){
		return needs;
	}

	public void setNeeds(HashMap<String, List<String>> needs){
		this.needs = needs;
	}

	public String getClientName(){
		return clientName;
	}

	public void setClientName(String clientName){
		this.clientName = clientName;
	}

	public String getRouterName(){
		return routerName;
	}

	public void setRouterName(String routerName){
		this.routerName = routerName;
	}

	public String getNodeName(){
		return nodeName;
	}

	public void setNodeName(String nodeName){
		this.nodeName = nodeName;
	}

	public C getClient(){
		return client;
	}

	public void setClient(C client){
		this.client = client;
	}

	public Router getRouter(){
		return router;
	}

	public void setRouter(Router router){
		this.router = router;
	}

	public Node<?, ?> getNode(){
		return node;
	}

	public void setNode(Node<?, ?> node){
		this.node = node;
	}
}
