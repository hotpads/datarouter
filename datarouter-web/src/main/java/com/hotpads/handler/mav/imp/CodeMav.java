package com.hotpads.handler.mav.imp;

import com.hotpads.handler.mav.Mav;

public class CodeMav extends Mav{
	
	public static final String 
		JSP_PATH = "/jsp/generic/code.jsp",
		VAR_NAME = "code";

	public CodeMav(){
		super(JSP_PATH);
	}
	
	public CodeMav(String code){
		super(JSP_PATH);
		put(VAR_NAME, code);
	}
	
}
