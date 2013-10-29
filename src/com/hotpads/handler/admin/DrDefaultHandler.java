package com.hotpads.handler.admin;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;

public class DrDefaultHandler extends BaseHandler{

	@Override
	@Handler
	protected Mav handleDefault(){
		return new Mav("/jsp/admin/index.jsp");
	}
}
