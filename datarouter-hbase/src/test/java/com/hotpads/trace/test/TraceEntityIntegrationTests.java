package com.hotpads.trace.test;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;
import com.hotpads.trace.node.TraceSubNodes;

@Guice(moduleFactory = DatarouterStorageTestModuleFactory.class)
public class TraceEntityIntegrationTests{

	private final Datarouter datarouter;

	private final TraceSubNodes nodes;

	@Inject
	public TraceEntityIntegrationTests(Datarouter datarouter, TraceTestRouter router){
		this.datarouter = datarouter;
		this.nodes = router.traceEntity();
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	private void resetTable(){
		nodes.trace().deleteAll(null);
		nodes.traceThread().deleteAll(null);
		nodes.traceSpan().deleteAll(null);
		nodes.trace().putMulti(TraceTestDataGenerator.traces, null);
		nodes.traceThread().putMulti(TraceTestDataGenerator.threads, null);
		nodes.traceSpan().putMulti(TraceTestDataGenerator.spans, null);
	}

	@Test
	public void testCounts(){
		resetTable();
		long numTraces = nodes.trace().stream(null, null).count();
		Assert.assertEquals(TraceTestDataGenerator.traces.size(), numTraces);
		long numThreads = nodes.traceThread().stream(null, null).count();
		Assert.assertEquals(TraceTestDataGenerator.threads.size(), numThreads);
		long numSpans = nodes.traceSpan().stream(null, null).count();
		Assert.assertEquals(TraceTestDataGenerator.spans.size(), numSpans);
	}

}
