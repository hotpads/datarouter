package com.hotpads.datarouter.client.imp.memcached.web;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.hotpads.datarouter.client.imp.memcached.client.MemcachedClientImp;
import com.hotpads.datarouter.client.imp.memcached.client.MemcachedStateException;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.routing.RouterParams;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.admin.RoutersHandler;
import com.hotpads.handler.mav.Mav;

public class MemcachedHandler extends BaseHandler {

	@Inject
	private DatarouterContext datarouterContext;

	private RouterParams<MemcachedClientImp> paramsRouter;

	@Handler
	protected Mav inspectClient() {
		initialize();
		Mav mav = new Mav(
				"/jsp/admin/datarouter/memcached/memcachedClientSummary.jsp");
		mav.put("client", paramsRouter.getClient().getSpyClient());
		Map<SocketAddress, Map<String, String>> stats = null;
		try {
			stats = paramsRouter.getClient().getSpyClient().getStats();
			mav.put("memcachedStats", stats);
		} catch (MemcachedStateException e) {
			logger.error("",e);
		}

		return mav;
	}

	private void initialize() {
		paramsRouter = new RouterParams<>(datarouterContext, params,
				MEMCHACHED_NEEDS);

	}

	private static final List<String> NEEDS_CLIENT = new ArrayList<>();
	static {
		NEEDS_CLIENT.add(RoutersHandler.ACTION_inspectClient);

	}

	private static final List<String> NEEDS_ROUTER = new ArrayList<>();
	static {
		NEEDS_ROUTER.addAll(NEEDS_CLIENT);
	}

	private static final List<String> NEEDS_NODE = new ArrayList<>();
	static {
	}

	private static final HashMap<String, List<String>> MEMCHACHED_NEEDS = new HashMap<>();
	static {
		MEMCHACHED_NEEDS.put(RouterParams.NEEDS_CLIENT, NEEDS_CLIENT);
		MEMCHACHED_NEEDS.put(RouterParams.NEEDS_ROUTER, NEEDS_ROUTER);
		MEMCHACHED_NEEDS.put(RouterParams.NEEDS_NODE, NEEDS_NODE);
	}

}