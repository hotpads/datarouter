package com.hotpads.handler.admin.client.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import com.hotpads.datarouter.client.imp.memory.MemoryClient;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.routing.RouterParams;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.admin.RoutersHandler;
import com.hotpads.handler.mav.Mav;

public class MemoryHandler extends BaseHandler {
	
	/**************** static **********************/

	private static final List<String> NEEDS_CLIENT = new ArrayList<>();
	static{
		NEEDS_CLIENT.add(RoutersHandler.ACTION_inspectClient);
	}

	private static final List<String> NEEDS_ROUTER = new ArrayList<>();
	static{
		NEEDS_ROUTER.addAll(NEEDS_CLIENT);
		NEEDS_ROUTER.add(RoutersHandler.ACTION_inspectRouter);
	}

	private static final List<String> NEEDS_NODE = new ArrayList<>();
	static{
	}

	private static final HashMap<String,List<String>> MEMORY_NEEDS = new HashMap<>();
	static{
		MEMORY_NEEDS.put(RouterParams.NEEDS_CLIENT, NEEDS_CLIENT);
		MEMORY_NEEDS.put(RouterParams.NEEDS_ROUTER, NEEDS_ROUTER);
		MEMORY_NEEDS.put(RouterParams.NEEDS_NODE, NEEDS_NODE);
	}
	
	
	/******************** fields ************************/
	
	@Inject
	private Datarouter datarouter;

	private RouterParams<MemoryClient> routerParams;

	@Handler
	protected Mav inspectClient() {
		initialize();
		Mav mav = new Mav("/jsp/admin/datarouter/memory/memoryClientSummary.jsp");
		MemoryClient client = routerParams.getClient();
		mav.put("nodes", routerParams.getNodes().getPhysicalNodesForClient(client.getName()));
		return mav;
	}

	private void initialize(){
		routerParams = new RouterParams<>(datarouter, params, MEMORY_NEEDS);
	}
}
