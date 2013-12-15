package com.hotpads.datarouter.op.executor;


public interface SessionExecutor<T>{
	
	void openSessions();
	void flushSessions();
	void cleanupSessions();
	
}
