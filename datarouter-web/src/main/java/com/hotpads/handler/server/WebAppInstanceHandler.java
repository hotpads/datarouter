package com.hotpads.handler.server;

import java.util.Collection;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.server.WebAppInstanceRouter;
import com.hotpads.server.databean.WebAppInstance;

public class WebAppInstanceHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(WebAppInstanceHandler.class);

	@Inject
	WebAppInstanceRouter webAppInstanceRouter;

	@Override
	protected Mav handleDefault(){
		Mav mav = new Mav("/jsp/server/webApps.jsp");

		Collection<WebAppInstance> webApps = DrListTool.createArrayList(webAppInstanceRouter.webApp.scan(null, null));
		mav.put("webAppInstances", webApps);

		return mav;
	}

}
