package com.hotpads.handler.mav.imp;

/**
 * @deprecated use a {@link com.hotpads.handler.encoder.JsonEncoder} instead
 */
@Deprecated
public class ErrorJsonMav extends JsonMav{

	public ErrorJsonMav(String message){
		super(message);
		this.setStatusCode(400);
	}
}
