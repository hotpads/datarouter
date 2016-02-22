package com.hotpads.datarouter.client.imp.hibernate.web;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;

public class DatarouterHibernateDispatcher extends BaseDispatcher{

	public static final String
			PREFIX = DatarouterWebDispatcher.URL_DATAROUTER + DatarouterWebDispatcher.CLIENTS,
			HIBERNATE = "/hibernate",
			PATH = PREFIX + HIBERNATE;

	public DatarouterHibernateDispatcher(DatarouterInjector injector, String servletContextPath){
		super(injector, servletContextPath, PATH);

		//All urls must start with PATH
		handle(PATH).withHandler(HibernateHandler.class);
	}

}
