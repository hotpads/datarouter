package com.hotpads.handler.mav.imp;

import com.hotpads.handler.mav.Mav;

public class GlobalRedirectMav extends Mav{

	public GlobalRedirectMav(){
		super();
	}
	
	public GlobalRedirectMav(String url){
		super();
		super.setRedirect(true);
		super.setGlobalRedirectUrl(url);
	}
	
}
