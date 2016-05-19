package com.hotpads.handler.mav.imp;

import com.hotpads.handler.mav.Mav;

public class GlobalRedirectMav extends Mav{

	public GlobalRedirectMav(String url){
		setGlobalRedirectUrl(url);
	}

}
