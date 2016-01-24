package com.hotpads.datarouter.client.imp.jdbc.web;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;

public class JdbcDatarouterDispatcher extends BaseDispatcher{

	public static final String
			PREFIX = DatarouterWebDispatcher.URL_DATAROUTER + DatarouterWebDispatcher.CLIENTS,
			JDBC = "/jdbc",
			PATH = PREFIX + JDBC;

	public JdbcDatarouterDispatcher(DatarouterInjector injector, String servletContextPath, String urlPrefix){
		super(injector, servletContextPath, urlPrefix);
		handle(PATH).withHandler(JdbcHandler.class);
	}

}
