package com.hotpads.datarouter.client.imp.jdbc.web;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;

public class DatarouterJdbcDispatcher extends BaseDispatcher{

	public static final String
			PREFIX = DatarouterWebDispatcher.URL_DATAROUTER + DatarouterWebDispatcher.CLIENTS,
			JDBC = "/jdbc",
			PATH = PREFIX + JDBC;

	public DatarouterJdbcDispatcher(DatarouterInjector injector, String servletContextPath){
		super(injector, servletContextPath, PATH);

		//All urls must start with PATH
		handle(PATH).withHandler(JdbcHandler.class);
	}

}
