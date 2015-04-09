package com.hotpads.datarouter.client.imp.hibernate;

import com.hotpads.DatarouterInjector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.dispatcher.DatarouterDispatcher;

public class HibernateDatarouterDispatcher extends BaseDispatcher{

	public static final String
			PREFIX = DatarouterDispatcher.URL_DATAROUTER + DatarouterDispatcher.CLIENTS,
			HIBERNATE = "/hibernate",
			PATH = PREFIX + HIBERNATE;

	public HibernateDatarouterDispatcher(DatarouterInjector injector, String servletContextPath, String urlPrefix){
		super(injector, servletContextPath, urlPrefix);
		handle(PATH).withHandler(HibernateHandler.class);
	}

}
