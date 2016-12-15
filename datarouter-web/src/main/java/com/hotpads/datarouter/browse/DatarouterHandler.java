package com.hotpads.datarouter.browse;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.InContextRedirectMav;

public class DatarouterHandler extends BaseHandler {

	@Override
	@Handler
	protected Mav handleDefault(){
		return new InContextRedirectMav(params, DatarouterWebDispatcher.PATH_routers);
	}

}
