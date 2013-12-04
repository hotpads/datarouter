package com.hotpads.handler.admin.client.memory;

import java.util.HashMap;
import java.util.List;

import com.google.inject.Inject;
import com.hotpads.datarouter.client.imp.memory.MemoryClient;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.routing.RouterParams;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.admin.RoutersHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public class MemoryHandler extends BaseHandler {
	@Inject
	private DataRouterContext dataRouterContext;

	private RouterParams<MemoryClient> paramsRouter;

	@Handler
	protected Mav inspectClient() {
		initialize();
		Mav mav = new Mav(
				"/jsp/admin/datarouter/memory/memoryClientSummary.jsp");
		mav.put("nodes", paramsRouter.getClient().getNodes());
		return mav;
	}

	private void initialize() {
		paramsRouter = new RouterParams<>(dataRouterContext, params,
				MEMORY_NEEDS);

	}

	protected static String ACTION_exportNodeToHFile = "exportNodeToHFile",
			ACTION_moveRegionsToCorrectServer = "moveRegionsToCorrectServer";

	private static final List<String> NEEDS_CLIENT = ListTool.create();
	static {
		NEEDS_CLIENT.add(ACTION_moveRegionsToCorrectServer);
		NEEDS_CLIENT.add(RoutersHandler.ACTION_inspectClient);

	}

	private static final List<String> NEEDS_ROUTER = ListTool.create();
	static {
		NEEDS_ROUTER.addAll(NEEDS_CLIENT);
		NEEDS_ROUTER.add(RoutersHandler.ACTION_inspectRouter);
		NEEDS_ROUTER.add(ACTION_exportNodeToHFile);
	}

	private static final List<String> NEEDS_NODE = ListTool.create();
	static {
		NEEDS_NODE.add(ACTION_exportNodeToHFile);
		NEEDS_NODE.add(ACTION_moveRegionsToCorrectServer);
	}

	private static final HashMap<String, List<String>> MEMORY_NEEDS = MapTool
			.createHashMap();
	static {
		MEMORY_NEEDS.put(RouterParams.NEEDS_CLIENT, NEEDS_CLIENT);
		MEMORY_NEEDS.put(RouterParams.NEEDS_ROUTER, NEEDS_ROUTER);
		MEMORY_NEEDS.put(RouterParams.NEEDS_NODE, NEEDS_NODE);
	}
}
