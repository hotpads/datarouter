package com.hotpads.trace.test;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.trace.Trace;
import com.hotpads.trace.TraceEntityNode;
import com.hotpads.trace.TraceSpan;
import com.hotpads.trace.TraceThread;
import com.hotpads.util.core.ListTool;


public class TraceEntityTests{

	private TraceTestRouter router;
	private TraceEntityNode node;
	
	public TraceEntityTests(){
		this.router = new TraceTestRouter();
		this.node = router.traceEntity();
		resetTable();
	}
	
	private void resetTable(){
		node.trace.deleteAll(null);
		node.thread.deleteAll(null);
		node.span.deleteAll(null);
		node.trace.putMulti(TraceTestDataGenerator.traces, null);
		node.thread.putMulti(TraceTestDataGenerator.threads, null);
		node.span.putMulti(TraceTestDataGenerator.spans, null);
	}
	
	@Test
	public void testCounts(){
		List<Trace> traces = ListTool.createArrayList(node.trace.scan(null, null));
		Assert.assertEquals(TraceTestDataGenerator.traces.size(), traces.size());
		List<TraceThread> traceThreads = ListTool.createArrayList(node.thread.scan(null, null));
		Assert.assertEquals(TraceTestDataGenerator.threads.size(), traceThreads.size());
		List<TraceSpan> traceSpans = ListTool.createArrayList(node.span.scan(null, null));
		Assert.assertEquals(TraceTestDataGenerator.spans.size(), traceSpans.size());
	}

}
