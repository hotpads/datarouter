package com.hotpads.datarouter.client.imp.hbase.web;

import com.hotpads.DatarouterInjector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.dispatcher.DatarouterDispatcher;

public class HBaseDatarouterDispatcher extends BaseDispatcher{

	public static final String
			PREFIX = DatarouterDispatcher.URL_DATAROUTER + DatarouterDispatcher.CLIENTS,
			HBASE = "/hbase",
			PATH = PREFIX + HBASE;

	public HBaseDatarouterDispatcher(DatarouterInjector injector, String servletContextPath, String urlPrefix){
		super(injector, servletContextPath, urlPrefix);
		handle(PATH).withHandler(HBaseHandler.class);
	}

}
