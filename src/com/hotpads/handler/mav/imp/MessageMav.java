package com.hotpads.handler.mav.imp;

import com.hotpads.handler.mav.Mav;

public class MessageMav extends Mav{
	
	public static final String 
		JSP_PATH = "/generic/message",
		VAR_NAME = "message";

	public MessageMav(){
		super(JSP_PATH);
	}
	
	public MessageMav(String message){
		super(JSP_PATH);
		addObject(VAR_NAME, message);
	}
	
}
