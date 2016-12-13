package com.hotpads.datarouter.client.imp.redis.web;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.dispatcher.BaseDispatcher;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;

public class DatarouterRedisDispatcher extends BaseDispatcher{

	public static final String
			PREFIX = DatarouterWebDispatcher.PATH_clients,
			REDIS = "/redis",
			PATH = PREFIX + REDIS;

	public DatarouterRedisDispatcher(DatarouterInjector injector, String servletContextPath){
		super(injector, servletContextPath, PATH);
		handle(PATH).withHandler(RedisHandler.class);
	}
}