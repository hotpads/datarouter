package com.hotpads.trace;

import java.util.List;

//naming of this class and its method is temporary.  will be migrated to better names
public interface Tracer{

	String getServerName();
	Long getTraceId();
	List<TraceThread> getThreads();
	List<TraceSpan> getSpans();
	
	Long getCurrentThreadId();
	
	void createAndStartThread(String name);
	void createThread(String name);
	void startThread();
	void appendToThreadName(String text);
	void appendToThreadInfo(String text);
	void finishThread();
	
	void startSpan(String name);
	void appendToSpanName(String text);
	void appendToSpanInfo(String text);
	void finishSpan();
	
}
