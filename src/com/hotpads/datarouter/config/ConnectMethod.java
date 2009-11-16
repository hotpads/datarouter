package com.hotpads.datarouter.config;

/*
 * Method for obtaining a database connection from a client
 */
public enum ConnectMethod {

	tryExisting,  //same as Participation.supports, usually the default
	requireExisting,
	tryExistingHandle,
	requireExistingHandle,
	
	requireNew,
	;
	
	
}
