package com.hotpads.trace;

import java.util.List;

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
import com.hotpads.util.core.java.ReflectionTool;

public class TraceEntityNode extends BaseEntityNode<TraceEntityKey,TraceEntity>{

	public SortedMapStorageNode<TraceKey,Trace> trace;
	public SortedMapStorageNode<TraceThreadKey,TraceThread> traceThread;
	public SortedMapStorageNode<TraceSpanKey,TraceSpan> traceSpan;
	
	public TraceEntityNode(String name, DataRouter router, String clientName){
		super(name);
		initNodes(router, clientName);
	}
	
	private void initNodes(DataRouter router, String clientName){
		trace = BaseDataRouter.cast(router.register(NodeFactory.create(clientName, 
				Trace.class, TraceFielder.class, router)));
		register(trace);
		
		traceThread = BaseDataRouter.cast(router.register(NodeFactory.create(clientName, 
				TraceThread.class, TraceThreadFielder.class, router)));
		register(traceThread);
		
		traceSpan = BaseDataRouter.cast(router.register(NodeFactory.create(clientName, 
				TraceSpan.class, TraceSpanFielder.class, router)));
		register(traceSpan);	
	}
	
	
	@Override
	public TraceEntity getEntity(TraceEntityKey key){
		TraceEntity entity = new TraceEntity(key);
		
		TraceKey tracePrefix = ReflectionTool.create(TraceKey.class).prefixFromEntityKey(key);
		List<Trace> traces = trace.getWithPrefix(tracePrefix, false, null);
		entity.add(trace, traces);
		
		TraceThreadKey traceThreadPrefix = ReflectionTool.create(TraceThreadKey.class).prefixFromEntityKey(key);
		List<TraceThread> traceThreads = traceThread.getWithPrefix(traceThreadPrefix, false, null);
		entity.add(traceThread, traceThreads);
		
		TraceSpanKey traceSpanPrefix = ReflectionTool.create(TraceSpanKey.class).prefixFromEntityKey(key);
		List<TraceSpan> traceSpans = traceSpan.getWithPrefix(traceSpanPrefix, false, null);
		entity.add(traceSpan, traceSpans);
		
		return entity;
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