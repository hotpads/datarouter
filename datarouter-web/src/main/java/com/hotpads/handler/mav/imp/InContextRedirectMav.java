package com.hotpads.handler.mav.imp;

import com.hotpads.handler.Params;
import com.hotpads.handler.mav.Mav;

public class InContextRedirectMav extends Mav{

	public InContextRedirectMav(Params params, String inContextUrl){
		setGlobalRedirectUrl(params.getContextPath() + inContextUrl);
	}

}
