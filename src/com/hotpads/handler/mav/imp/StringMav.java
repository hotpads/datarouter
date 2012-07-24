package com.hotpads.handler.mav.imp;

import com.hotpads.handler.mav.Mav;

public class StringMav extends Mav{

	public static final String
		JSP_PATH = "/generic/string",
		VAR_NAME = "string";

	public StringMav(){
		super(JSP_PATH);
	}

	public StringMav(String string){
		super(JSP_PATH);
		addObject(VAR_NAME, string);
	}
	
}
