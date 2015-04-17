package com.hotpads.datarouter.client.imp.jdbc.web;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;
import javax.inject.Inject;
import com.hotpads.datarouter.client.imp.jdbc.JdbcClientImp;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.routing.RouterParams;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.admin.RoutersHandler;
import com.hotpads.handler.mav.Mav;

public class JdbcHandler extends BaseHandler {
	
	/*************** constants *******************/

	private static final List<String> NEEDS_CLIENT = ImmutableList.of(RoutersHandler.ACTION_inspectClient);
	private static final List<String> NEEDS_ROUTER = NEEDS_CLIENT;

	private static final HashMap<String, List<String>> JDBC_NEEDS = new HashMap<>();
	static {
		JDBC_NEEDS.put(RouterParams.NEEDS_ROUTER, NEEDS_ROUTER);
		JDBC_NEEDS.put(RouterParams.NEEDS_CLIENT, NEEDS_CLIENT);
	}
	
	/***************** fields ********************/
	
	@Inject
	private DatarouterContext datarouterContext;

	private RouterParams<JdbcClientImp> paramsRouter;

	
	/**************** methods ********************/
	
	private void initialize() {
		paramsRouter = new RouterParams<>(datarouterContext, params, JDBC_NEEDS);
	}

	@Handler
	protected Mav inspectClient(){
		initialize();
		Mav mav = new Mav("/jsp/admin/datarouter/jdbc/jdbcClientSummary.jsp");
		mav.put("clientStats", paramsRouter.getClient().getStats());
		mav.put("nodes", paramsRouter.getContext().getNodes().getPhysicalNodesForClient(paramsRouter.getClientName()));
		return mav;
	}

}
