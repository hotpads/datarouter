package com.hotpads.datarouter.op.executor;


public interface SessionExecutor{
	
	void openSessions();
	void flushSessions();
	void cleanupSessions();
	
}
