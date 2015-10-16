package com.hotpads.datarouter.client.imp.memcached.web;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.dispatcher.DatarouterDispatcher;

public class MemcachedDatarouterDispatcher extends BaseDispatcher{

	public static final String
			PREFIX = DatarouterDispatcher.URL_DATAROUTER + DatarouterDispatcher.CLIENTS,
			MEMCACHED = "/memcached",
			PATH = PREFIX + MEMCACHED;

	public MemcachedDatarouterDispatcher(DatarouterInjector injector, String servletContextPath, String urlPrefix){
		super(injector, servletContextPath, urlPrefix);
		handle(PATH).withHandler(MemcachedHandler.class);
	}

}
