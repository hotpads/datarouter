package com.hotpads.handler.mav.imp;

public class ErrorJsonMav extends JsonMav{

	public ErrorJsonMav(String message){
		super(message);
		this.setStatusCode(400);
	}
}
