package com.hotpads.handler.mav.imp;

import net.sf.json.JSON;

import com.hotpads.handler.mav.Mav;

public class JsonMav extends Mav{
	
	public static final String 
		JSP_PATH = "/jsp/generic/json.jsp",
		VAR_NAME = "json";

	public JsonMav(){
		super(JSP_PATH);
	}
	
	public JsonMav(String data){
		super(JSP_PATH);
		put(VAR_NAME, data);
	}
	
	public JsonMav(JSON json){
		super(JSP_PATH);
		put(VAR_NAME, json.toString());
	}
	
}
