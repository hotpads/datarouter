package com.hotpads.handler.server;

import java.util.Collection;

import javax.inject.Inject;

import com.hotpads.datarouter.util.core.DrListTool;
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

		Collection<WebAppInstance> webApps = DrListTool.createArrayList(webAppNodes.getWebApps().scan(null, null));
		mav.put("webAppInstances", webApps);

		return mav;
	}

}
