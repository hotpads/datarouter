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
	private WebAppInstanceNodes webAppNodes;

	@Override
	protected Mav handleDefault(){
		Mav mav = new Mav("/jsp/server/webApps.jsp");

		Collection<WebAppInstance> webApps = webAppNodes.getWebApps().stream(null, null).collect(Collectors.toList());
		mav.put("webAppInstances", webApps);

		return mav;
	}

}
