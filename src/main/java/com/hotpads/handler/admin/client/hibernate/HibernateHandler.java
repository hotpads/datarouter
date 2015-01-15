package com.hotpads.handler.admin.client.hibernate;

import java.util.HashMap;
import java.util.List;

import com.google.inject.Inject;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientImp;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.routing.RouterParams;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.admin.RoutersHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public class HibernateHandler extends BaseHandler {
	@Inject
	private DatarouterContext datarouterContext;

	private RouterParams<HibernateClientImp> paramsRouter;

	@Handler
	protected Mav inspectClient() {
		initialize();
		Mav mav = new Mav(
				"/jsp/admin/datarouter/hibernate/hibernateClientSummary.jsp");
		mav.put("address", "TODO");
		mav.put("hibernateClientStats", paramsRouter.getClient().getStats());
		String[] tokens = paramsRouter.getClient().getSessionFactory()
				.getStatistics().toString().split(",");
		List<String[]> sessionFactoryStats = ListTool.create();
		for (String token : tokens) {
			sessionFactoryStats.add(token.split("="));
		}
		mav.put("sessionFactoryStats", sessionFactoryStats);
		mav.put("nodes", paramsRouter.getContext().getNodes().getPhysicalNodesForClient(paramsRouter.getClientName()));
		return mav;
	}

	private void initialize() {
		paramsRouter = new RouterParams<>(datarouterContext, params, HIBERNATE_NEEDS);

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

	private static final HashMap<String, List<String>> HIBERNATE_NEEDS = MapTool
			.createHashMap();
	static {
		HIBERNATE_NEEDS.put(RouterParams.NEEDS_CLIENT, NEEDS_CLIENT);
		HIBERNATE_NEEDS.put(RouterParams.NEEDS_ROUTER, NEEDS_ROUTER);
		HIBERNATE_NEEDS.put(RouterParams.NEEDS_NODE, NEEDS_NODE);
	}

}
