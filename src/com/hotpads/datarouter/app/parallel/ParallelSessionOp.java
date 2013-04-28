package com.hotpads.datarouter.app.parallel;


public interface ParallelSessionOp<T>{
	
	void openSessions();
	void flushSessions();
	void cleanupSessions();
	
}
