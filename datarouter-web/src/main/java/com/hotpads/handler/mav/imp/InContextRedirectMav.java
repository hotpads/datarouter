package com.hotpads.handler.mav.imp;

import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.params.Params;

public class InContextRedirectMav extends Mav{

	public InContextRedirectMav(Params params, String inContextUrl){
		setGlobalRedirectUrl(params.getContextPath() + inContextUrl);
	}

}
