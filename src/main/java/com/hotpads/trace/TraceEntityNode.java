package com.hotpads.trace;

import com.hotpads.datarouter.node.entity.BaseEntityNode;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.trace.Trace.TraceFielder;
import com.hotpads.trace.TraceSpan.TraceSpanFielder;
import com.hotpads.trace.TraceThread.TraceThreadFielder;
import com.hotpads.trace.key.TraceEntityKey;
import com.hotpads.trace.key.TraceKey;
import com.hotpads.trace.key.TraceSpanKey;
import com.hotpads.trace.key.TraceThreadKey;

public class TraceEntityNode extends BaseEntityNode<TraceEntityKey>{

	public MapStorageNode<TraceKey,Trace> trace;
	public SortedMapStorageNode<TraceThreadKey,TraceThread> traceThread;
	public SortedMapStorageNode<TraceSpanKey,TraceSpan> traceSpan;
	
	public TraceEntityNode(String name, DataRouter router, String clientName){
		super(name);
		initNodes(router, clientName);
	}
	
	private void initNodes(DataRouter router, String clientName){
		trace = BaseDataRouter.cast(router.register(NodeFactory.create(clientName, 
				Trace.class, TraceFielder.class, router)));
		register(TraceKey.class, trace);
		
		traceThread = BaseDataRouter.cast(router.register(NodeFactory.create(clientName, 
				TraceThread.class, TraceThreadFielder.class, router)));
		register(TraceThreadKey.class, traceThread);
		
		traceSpan = BaseDataRouter.cast(router.register(NodeFactory.create(clientName, 
				TraceSpan.class, TraceSpanFielder.class, router)));
		register(TraceSpanKey.class, traceSpan);	
	}
	
	
	/*********************** get nodes ******************************/

	public MapStorageNode<TraceKey,Trace> trace(){
		return trace;
	}

	public SortedMapStorageNode<TraceThreadKey,TraceThread> traceThread(){
		return traceThread;
	}

	public SortedMapStorageNode<TraceSpanKey,TraceSpan> traceSpan(){
		return traceSpan;
	}
	
	
	
}
