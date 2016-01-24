package com.hotpads.datarouter.client.imp.hibernate.web;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;

public class HibernateDatarouterDispatcher extends BaseDispatcher{

	public static final String
			PREFIX = DatarouterWebDispatcher.URL_DATAROUTER + DatarouterWebDispatcher.CLIENTS,
			HIBERNATE = "/hibernate",
			PATH = PREFIX + HIBERNATE;

	public HibernateDatarouterDispatcher(DatarouterInjector injector, String servletContextPath, String urlPrefix){
		super(injector, servletContextPath, urlPrefix);
		handle(PATH).withHandler(HibernateHandler.class);
	}

}
