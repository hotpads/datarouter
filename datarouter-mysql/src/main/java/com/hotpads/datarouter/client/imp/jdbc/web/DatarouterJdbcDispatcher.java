package com.hotpads.datarouter.client.imp.jdbc.web;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.dispatcher.BaseRouteSet;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;
import com.hotpads.handler.dispatcher.DispatchRule;
import com.hotpads.handler.user.role.DatarouterUserRole;

public class DatarouterJdbcDispatcher extends BaseRouteSet{

	public static final String
			PREFIX = DatarouterWebDispatcher.PATH_clients,
			JDBC = "/jdbc",
			PATH = PREFIX + JDBC;

	public DatarouterJdbcDispatcher(DatarouterInjector injector, String servletContextPath){
		super(injector, servletContextPath, PATH);

		//All urls must start with PATH
		handle(PATH).withHandler(JdbcHandler.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule.allowRoles(DatarouterUserRole.datarouterAdmin);
	}

}
