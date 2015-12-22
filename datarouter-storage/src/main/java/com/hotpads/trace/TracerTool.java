package com.hotpads.trace;

public class TracerTool{
	
	public static Long getTraceId(Tracer tracer){
		if(tracer == null){
			return null;
		}
		return tracer.getTraceId();
	}
	

	/***************** Thread methods *******************/
	
	public static Long getCurrentThreadId(Tracer tracer){
		if(tracer == null){
			return null;
		}
		return tracer.getCurrentThreadId();
	}
	
	
	public static void createAndStartThread(Tracer tracer, String name){
		if(tracer == null){
			return;
		}
		tracer.createAndStartThread(name);
	}
	
	
	public static void createThread(Tracer tracer, String name){
		if(tracer == null){
			return;
		}
		tracer.createThread(name);
	}
	
	
	public static void startThread(Tracer tracer){
		if(tracer == null){
			return;
		}
		tracer.startThread();
	}
	
	
	public static void appendToThreadName(Tracer tracer, String text){
		if(tracer == null){
			return;
		}
		tracer.appendToThreadName(text);
	}
	
	
	public static void appendToThreadInfo(Tracer tracer, String text){
		if(tracer == null){
			return;
		}
		tracer.appendToThreadInfo(text);
	}
	
	
	public static void finishThread(Tracer tracer){
		if(tracer == null){
			return;
		}
		tracer.finishThread();
	}
	
	
	/***************** Span methods ************************/
	
	public static void startSpan(Tracer tracer, String name){
		if(tracer == null){
			return;
		}
		tracer.startSpan(name);
	}
	
	
	public static void appendToSpanName(Tracer tracer, String text){
		if(tracer == null){
			return;
		}
		tracer.appendToSpanName(text);
	}
	
	
	public static void appendToSpanInfo(Tracer tracer, String text){
		if(tracer == null){
			return;
		}
		tracer.appendToSpanInfo(text);
	}
	
	
	public static void finishSpan(Tracer tracer){
		if(tracer == null){
			return;
		}
		tracer.finishSpan();
	}
	
}
