package com.hotpads.handler.mav.imp;

import com.hotpads.handler.mav.Mav;

public class StringMav extends Mav{

	public static final String
		JSP_PATH = "/jsp/generic/string.jsp",
		VAR_NAME = "string";

	public StringMav(){
		super(JSP_PATH);
	}

	public StringMav(String string){
		super(JSP_PATH);
		put(VAR_NAME, string);
	}
	
}
