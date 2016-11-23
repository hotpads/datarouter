package com.hotpads.handler.server;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.server.WebAppInstanceNodes;
import com.hotpads.server.databean.WebAppInstance;

public class WebAppInstanceHandler extends BaseHandler{
	@Inject
	private WebAppInstanceNodes webAppInstanceNodes;

	@Override
	protected Mav handleDefault(){
		Mav mav = new Mav("/jsp/server/webAppInstances.jsp");

		Collection<WebAppInstance> webAppInstances = webAppInstanceNodes.getWebAppInstance().stream(null, null)
				.collect(Collectors.toList());
		mav.put("webAppInstances", webAppInstances);

		return mav;
	}

}
