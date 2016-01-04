package com.hotpads.trace;

public class TracerThreadLocal{

	/*************** ThreadLocal ***************/
	
	private static final ThreadLocal<Tracer> tracerThreadLocal = new ThreadLocal<>();

	
	/************** methods *****************/
	
	public static Tracer bindToThread(Tracer tracer) {
		tracerThreadLocal.set(tracer);
		return tracer;
	}

	public static void clearFromThread() {
		tracerThreadLocal.set(null);
	}

	public static Tracer get() {
		return tracerThreadLocal.get();
	}

}
