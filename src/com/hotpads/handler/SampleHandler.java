package com.hotpads.handler;

public class SampleHandler extends BaseHandler{
	SampleHandler(){//no-arg for reflection
	}

//	@Override
//	String handles(){ return "/sample/test/url**"; }
		
	@Override//optional method
	boolean permitted(){
		return rejectMe();
	}
	
	
	/**************** handler methods ******************/
	
	@Override//default handler
	public void handleDefault(){
		p("you didn't specify any submitAction");
	}
	
	public void sayHello(){//called if submitAction=sayHello
		String stringToDisplay = "hello "+userId();
		p(stringToDisplay);
	}
	
	
	/*************** type safe param parsers ******************/
	
	//type-safe
	//don't have to pass around "HttpServletRequest request"
	
	Long userId(){ return params.requiredLong("userId"); }  
	Boolean rejectMe(){ return params.optionalBoolean("rejectMe", false); }
}
