package com.hotpads.datarouter.app.client.parallel;

import com.hotpads.datarouter.app.SessionApp;

public interface ParallelSessionApp<T> 
extends SessionApp<T> {
	
	void openSessions();
	void closeSessions();
	
}
