package com.hotpads.handler.mav.imp;

import com.hotpads.handler.Params;
import com.hotpads.handler.mav.Mav;

public class InContextRedirectMav extends Mav{

	public InContextRedirectMav(){
		super();
	}
	
	public InContextRedirectMav(Params params, String inContextUrl){
		super();
		super.setRedirect(true);
		super.setGlobalRedirectUrl(params.getContextPath() + inContextUrl);
	}
	
}
