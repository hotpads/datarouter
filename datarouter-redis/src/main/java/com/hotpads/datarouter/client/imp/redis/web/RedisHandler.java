package com.hotpads.datarouter.client.imp.redis.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import com.hotpads.datarouter.client.imp.redis.client.RedisClientImp;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.routing.RouterParams;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.admin.RoutersHandler;
import com.hotpads.handler.mav.Mav;

public class RedisHandler extends BaseHandler {

	@Inject
	private Datarouter datarouter;

	private RouterParams<RedisClientImp> paramsRouter;

	@Handler
	protected Mav inspectClient() {
		initialize();
		Mav mav = new Mav("/jsp/admin/datarouter/redis/redisClientSummary.jsp");
		mav.put("client", paramsRouter.getClient().getJedisClient());
		mav.put("redisStats", paramsRouter.getClient().getJedisClient().info());
		return mav;
	}

	private void initialize(){
		paramsRouter = new RouterParams<>(datarouter, params, REDIS_NEEDS);
	}

	private static final List<String> NEEDS_CLIENT = new ArrayList<>();
	static{
		NEEDS_CLIENT.add(RoutersHandler.ACTION_inspectClient);
	}

	private static final List<String> NEEDS_ROUTER = new ArrayList<>();
	static{
		NEEDS_ROUTER.addAll(NEEDS_CLIENT);
	}

	private static final List<String> NEEDS_NODE = new ArrayList<>();
	static{
	}

	private static final HashMap<String, List<String>> REDIS_NEEDS = new HashMap<>();
	static{
		REDIS_NEEDS.put(RouterParams.NEEDS_CLIENT, NEEDS_CLIENT);
		REDIS_NEEDS.put(RouterParams.NEEDS_ROUTER, NEEDS_ROUTER);
		REDIS_NEEDS.put(RouterParams.NEEDS_NODE, NEEDS_NODE);
	}
}