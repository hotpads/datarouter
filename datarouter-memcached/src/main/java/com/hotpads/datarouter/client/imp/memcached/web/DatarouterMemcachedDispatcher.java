package com.hotpads.datarouter.client.imp.memcached.web;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.dispatcher.BaseDispatcher;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;

public class DatarouterMemcachedDispatcher extends BaseDispatcher{

	public static final String
			PREFIX = DatarouterWebDispatcher.PATH_clients,
			MEMCACHED = "/memcached",
			PATH = PREFIX + MEMCACHED;

	public DatarouterMemcachedDispatcher(DatarouterInjector injector, String servletContextPath){
		super(injector, servletContextPath, PATH);

		//All urls must start with PATH
		handle(PATH).withHandler(MemcachedHandler.class);
	}

}
