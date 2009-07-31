package com.hotpads.datarouter.app.client.parallel;

import com.hotpads.datarouter.app.SessionApp;

public interface ParallelSessionApp<T> 
extends SessionApp<T> {
	
	void openSessions() throws Exception;
	void closeSessions() throws Exception;
	
}
