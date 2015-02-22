package com.hotpads.trace.test;

import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hotpads.datarouter.test.DatarouterTestInjectorProvider;
import com.hotpads.trace.Trace;
import com.hotpads.trace.TraceSpan;
import com.hotpads.trace.TraceThread;
import com.hotpads.trace.node.TraceCompoundNode;
import com.hotpads.trace.node.TraceEntityNode;
import com.hotpads.trace.node.TraceSubNodes;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.ObjectTool;


@RunWith(Parameterized.class)
public class TraceEntityTests{

	@Parameters
	public static Collection<Object[]> parameters(){
		List<Object[]> params = ListTool.create();
		params.add(new Object[]{TraceCompoundNode.class});
		params.add(new Object[]{TraceEntityNode.class});
		return params;
	}
	
	private TraceTestRouter router;
	private TraceSubNodes nodes;
	
//	public TraceEntityTests(){//required public no-arg
//	}
	
	public TraceEntityTests(Class<TraceSubNodes> nodesClass){
		Injector injector = new DatarouterTestInjectorProvider().get();
		this.router = injector.getInstance(TraceTestRouter.class);
		if(ObjectTool.equals(TraceCompoundNode.class, nodesClass)){
			this.nodes = router.traceCompound();
		}else if(ObjectTool.equals(TraceEntityNode.class, nodesClass)){
			this.nodes = router.traceEntity();
		}else{
			throw new IllegalArgumentException("unknown nodes class "+nodesClass);
		}
		resetTable();
	}
	
	private void resetTable(){
		nodes.trace().deleteAll(null);
		nodes.thread().deleteAll(null);
		nodes.span().deleteAll(null);
		nodes.trace().putMulti(TraceTestDataGenerator.traces, null);
		nodes.thread().putMulti(TraceTestDataGenerator.threads, null);
		nodes.span().putMulti(TraceTestDataGenerator.spans, null);
	}
	
	@Test
	public void testCounts(){
		List<Trace> traces = ListTool.createArrayList(nodes.trace().scan(null, null));
		Assert.assertEquals(TraceTestDataGenerator.traces.size(), traces.size());
		List<TraceThread> traceThreads = ListTool.createArrayList(nodes.thread().scan(null, null));
		Assert.assertEquals(TraceTestDataGenerator.threads.size(), traceThreads.size());
		List<TraceSpan> traceSpans = ListTool.createArrayList(nodes.span().scan(null, null));
		Assert.assertEquals(TraceTestDataGenerator.spans.size(), traceSpans.size());
	}

}
