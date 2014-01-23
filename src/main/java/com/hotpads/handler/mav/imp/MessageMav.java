package com.hotpads.handler.mav.imp;

import com.hotpads.handler.mav.Mav;

public class MessageMav extends Mav{
	
	public static final String 
		JSP_PATH = "/jsp/generic/message.jsp",
		VAR_NAME = "message";

	public MessageMav(){
		super(JSP_PATH);
	}
	
	public MessageMav(String message){
		super(JSP_PATH);
		put(VAR_NAME, message);
	}
	
}
