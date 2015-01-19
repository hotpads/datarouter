package com.hotpads.handler.admin.client.memory;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import com.hotpads.datarouter.client.imp.memory.MemoryClient;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.routing.RouterParams;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.admin.RoutersHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public class MemoryHandler extends BaseHandler {
	
	/**************** static **********************/

	private static final List<String> NEEDS_CLIENT = ListTool.create();
	static{
		NEEDS_CLIENT.add(RoutersHandler.ACTION_inspectClient);
	}

	private static final List<String> NEEDS_ROUTER = ListTool.create();
	static{
		NEEDS_ROUTER.addAll(NEEDS_CLIENT);
		NEEDS_ROUTER.add(RoutersHandler.ACTION_inspectRouter);
	}

	private static final List<String> NEEDS_NODE = ListTool.create();
	static{
	}

	private static final HashMap<String,List<String>> MEMORY_NEEDS = MapTool.createHashMap();
	static{
		MEMORY_NEEDS.put(RouterParams.NEEDS_CLIENT, NEEDS_CLIENT);
		MEMORY_NEEDS.put(RouterParams.NEEDS_ROUTER, NEEDS_ROUTER);
		MEMORY_NEEDS.put(RouterParams.NEEDS_NODE, NEEDS_NODE);
	}
	
	
	/******************** fields ************************/
	
	@Inject
	private DatarouterContext datarouterContext;

	private RouterParams<MemoryClient> routerParams;

	@Handler
	protected Mav inspectClient() {
		initialize();
		Mav mav = new Mav("/jsp/admin/datarouter/memory/memoryClientSummary.jsp");
		MemoryClient client = routerParams.getClient();
		List nodes = routerParams.getNodes().getPhysicalNodesForClient(client.getName());
		mav.put("nodes", nodes);
		return mav;
	}

	private void initialize(){
		routerParams = new RouterParams<>(datarouterContext, params, MEMORY_NEEDS);
	}
}
