package com.hotpads.handler.port;

import javax.servlet.ServletContext;

import com.google.inject.Provider;

public class PortIdentificatorProvider implements Provider<TomcatServerXmlPortIdentier>{

	protected ServletContext context;
	
	

	public PortIdentificatorProvider(ServletContext context){
		super();
		this.context = context;
	}



	@Override
	public TomcatServerXmlPortIdentier get(){
		try{
			return new TomcatServerXmlPortIdentier(context);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

}
