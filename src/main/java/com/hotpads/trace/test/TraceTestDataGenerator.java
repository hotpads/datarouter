package com.hotpads.trace.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import com.hotpads.trace.Trace;
import com.hotpads.trace.TraceSpan;
import com.hotpads.trace.TraceThread;

public class TraceTestDataGenerator{

	public static List<Trace> traces = new ArrayList<>();
	public static List<TraceThread> threads = new ArrayList<>();
	public static List<TraceSpan> spans = new ArrayList<>();
	
	static{
		Trace trace1 = new Trace();
		traces.add(trace1);
		
		TraceThread thread1a = new TraceThread(trace1.getId(), false);//only one thread should have hasParent=false.  gets id 0
		threads.add(thread1a);
		Assert.assertEquals(trace1.getId(), thread1a.getTraceId());
		TraceSpan span1a1 = new TraceSpan(thread1a.getTraceId(), thread1a.getId(), 1, 1);
		spans.add(span1a1);
		TraceSpan span1a2 = new TraceSpan(thread1a.getTraceId(), thread1a.getId(), 2, 1);
		spans.add(span1a2);
		
		TraceThread thread1b = new TraceThread(trace1.getId(), true);
		threads.add(thread1b);
		Assert.assertEquals(trace1.getId(), thread1b.getTraceId());
		TraceSpan span1b1 = new TraceSpan(thread1b.getTraceId(), thread1b.getId(), 1, 1);
		spans.add(span1b1);
		TraceSpan span1b2 = new TraceSpan(thread1b.getTraceId(), thread1b.getId(), 2, 1);
		spans.add(span1b2);
		TraceSpan span1b3 = new TraceSpan(thread1b.getTraceId(), thread1b.getId(), 3, 1);
		spans.add(span1b3);
		Assert.assertEquals(trace1.getId(), span1b3.getTraceId());
		
		for(Trace trace : traces){
			trace.setContext("blah");
			trace.setDuration(123L);
			trace.setDurationNano(123*1000*1000L);
			trace.setParams("paramA=a&paramB=b");
			trace.setSessionId("alskdbljkqwerkjbkqbjkfqwef");
			trace.setType("mighty");
		}
		
		for(TraceThread thread : threads){
			thread.setInfo("the info");
			thread.setName("my name");
			thread.setNanoStart(8888888888888L);
			thread.setParentId(2222L);
			thread.setQueuedDuration(111L);
			thread.setQueuedDurationNano(111000000L);
			thread.setRunningDuration(33333L);
			thread.setRunningDurationNano(33333000000L);
			thread.setServerId("el server");
		}
		
		for(TraceSpan span : spans){
			span.setDuration(321L);
			span.setDurationNano(321000000L);
			span.setInfo("the info is lost");
			span.setName("phillip");
		}
		
	}
	
}
