package com.hotpads.datarouter.client.imp.hibernate.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;
import javax.inject.Inject;
import com.hotpads.datarouter.client.imp.hibernate.client.HibernateClientImp;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.routing.RouterParams;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.admin.RoutersHandler;
import com.hotpads.handler.mav.Mav;

public class HibernateHandler extends BaseHandler {
	
	/*************** constants *******************/

	private static final List<String> NEEDS_CLIENT = ImmutableList.of(RoutersHandler.ACTION_inspectClient);
	private static final List<String> NEEDS_ROUTER = NEEDS_CLIENT;

	private static final HashMap<String, List<String>> HIBERNATE_NEEDS = new HashMap<>();
	static {
		HIBERNATE_NEEDS.put(RouterParams.NEEDS_ROUTER, NEEDS_ROUTER);
		HIBERNATE_NEEDS.put(RouterParams.NEEDS_CLIENT, NEEDS_CLIENT);
	}
	
	/***************** fields ********************/
	
	@Inject
	private DatarouterContext datarouterContext;

	private RouterParams<HibernateClientImp> paramsRouter;

	
	/**************** methods ********************/
	
	private void initialize() {
		paramsRouter = new RouterParams<>(datarouterContext, params, HIBERNATE_NEEDS);
	}

	@Handler
	protected Mav inspectClient(){
		initialize();
		Mav mav = new Mav("/jsp/admin/datarouter/hibernate/hibernateClientSummary.jsp");
		mav.put("hibernateClientStats", paramsRouter.getClient().getStats());
		String[] tokens = paramsRouter.getClient().getSessionFactory().getStatistics().toString().split(",");
		List<String[]> sessionFactoryStats = new ArrayList<>();
		for (String token : tokens) {
			sessionFactoryStats.add(token.split("="));
		}
		mav.put("sessionFactoryStats", sessionFactoryStats);
		mav.put("nodes", paramsRouter.getContext().getNodes().getPhysicalNodesForClient(paramsRouter.getClientName()));
		return mav;
	}

}
