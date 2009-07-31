package com.hotpads.datarouter.app;

public interface App<T> {

	T runInEnvironment() throws Exception;
	
}
