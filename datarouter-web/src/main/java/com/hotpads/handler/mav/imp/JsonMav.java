package com.hotpads.handler.mav.imp;

import com.hotpads.handler.mav.Mav;

import net.sf.json.JSON;

/**
 * @deprecated use a {@link com.hotpads.handler.encoder.JsonEncoder} instead
 */
@Deprecated
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

	public JsonMav(JSON json, int indentation){
		super(JSP_PATH);
		put(VAR_NAME, json.toString(indentation));
	}

}
