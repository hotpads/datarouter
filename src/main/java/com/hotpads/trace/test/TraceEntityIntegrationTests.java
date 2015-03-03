package com.hotpads.trace.test;

import java.util.List;

import javax.inject.Inject;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.test.DatarouterTestModuleFactory;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.trace.Trace;
import com.hotpads.trace.TraceSpan;
import com.hotpads.trace.TraceThread;
import com.hotpads.trace.node.TraceCompoundNode;
import com.hotpads.trace.node.TraceEntityNode;
import com.hotpads.trace.node.TraceSubNodes;

@Guice(moduleFactory=DatarouterTestModuleFactory.class)
public class TraceEntityIntegrationTests{//Should it be an integration test ?

	private static final String PARAMETERS = "parameters";

	@DataProvider(name=PARAMETERS)
	public static Object[][] parameters(){
		Object[][] params = new Object[][]{
				{TraceCompoundNode.class},
				{TraceEntityNode.class}
		};
		return params;
	}

	@Inject
	private TraceTestRouter router;

	private TraceSubNodes nodes;

	private void initNodes(Class<? extends TraceSubNodes> type){
		if(DrObjectTool.equals(TraceCompoundNode.class, type)){
			nodes = router.traceCompound();
		}else if(DrObjectTool.equals(TraceEntityNode.class, type)){
			nodes = router.traceEntity();
		}else{
			throw new IllegalArgumentException("unknown nodes class " + type);
		}
	}

	private void resetTable(){
		nodes.trace().deleteAll(null);
		nodes.thread().deleteAll(null);
		nodes.span().deleteAll(null);
		nodes.trace().putMulti(TraceTestDataGenerator.traces, null);
		nodes.thread().putMulti(TraceTestDataGenerator.threads, null);
		nodes.span().putMulti(TraceTestDataGenerator.spans, null);
	}

	@Test(dataProvider=PARAMETERS)
	public void testCounts(Class<? extends TraceSubNodes> type){
		initNodes(type);
		resetTable();
		List<Trace> traces = DrListTool.createArrayList(nodes.trace().scan(null, null));
		AssertJUnit.assertEquals(TraceTestDataGenerator.traces.size(), traces.size());
		List<TraceThread> traceThreads = DrListTool.createArrayList(nodes.thread().scan(null, null));
		AssertJUnit.assertEquals(TraceTestDataGenerator.threads.size(), traceThreads.size());
		List<TraceSpan> traceSpans = DrListTool.createArrayList(nodes.span().scan(null, null));
		AssertJUnit.assertEquals(TraceTestDataGenerator.spans.size(), traceSpans.size());
	}

}
