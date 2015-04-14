package com.hotpads.datarouter.client.imp.jdbc.web;

import com.hotpads.DatarouterInjector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.dispatcher.DatarouterDispatcher;

public class JdbcDatarouterDispatcher extends BaseDispatcher{

	public static final String
			PREFIX = DatarouterDispatcher.URL_DATAROUTER + DatarouterDispatcher.CLIENTS,
			JDBC = "/jdbc",
			PATH = PREFIX + JDBC;

	public JdbcDatarouterDispatcher(DatarouterInjector injector, String servletContextPath, String urlPrefix){
		super(injector, servletContextPath, urlPrefix);
		handle(PATH).withHandler(JdbcHandler.class);
	}

}
