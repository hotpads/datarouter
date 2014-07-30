package com.hotpads.logging;

import java.net.URI;

import org.apache.logging.log4j.core.LoggerContext;

public class StartupLoggerContext extends LoggerContext{

	public StartupLoggerContext(String name, Object externalContext, URI configLocn){
		super(name, externalContext, configLocn);
		System.out.println("ici");
	}

}
