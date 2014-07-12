package com.hotpads.trace.node;

import java.util.List;

import com.hotpads.datarouter.client.imp.hbase.task.HBaseTaskNameParams;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.entity.BaseEntityNode;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.trace.Trace;
import com.hotpads.trace.Trace.TraceFielder;
import com.hotpads.trace.TraceEntity;
import com.hotpads.trace.TraceSpan;
import com.hotpads.trace.TraceSpan.TraceSpanFielder;
import com.hotpads.trace.TraceThread;
import com.hotpads.trace.TraceThread.TraceThreadFielder;
import com.hotpads.trace.key.TraceEntityKey;
import com.hotpads.trace.key.TraceKey;
import com.hotpads.trace.key.TraceSpanKey;
import com.hotpads.trace.key.TraceThreadKey;
import com.hotpads.util.core.java.ReflectionTool;

public class TraceCompoundNode 
extends BaseEntityNode<TraceEntityKey,TraceEntity>
implements TraceNodes{
	
	private static final String
		NODE_NAME = "TraceCompoundNode",
		TABLE_Trace = "TestTrace",
		TABLE_TraceThread = "TestTraceThread",
		TABLE_TraceSpan = "TestTraceSpan";

	public SortedMapStorageNode<TraceKey,Trace> trace;
	public SortedMapStorageNode<TraceThreadKey,TraceThread> thread;
	public SortedMapStorageNode<TraceSpanKey,TraceSpan> span;
	
	public TraceCompoundNode(String name, DataRouter router, String clientName){
		super(router.getContext(), new HBaseTaskNameParams(clientName, TABLE_Trace, NODE_NAME));
		initNodes(router, clientName);
	}
	
	private void initNodes(DataRouter router, String clientName){
		trace = BaseDataRouter.cast(router.register(NodeFactory.create(clientName, 
				TABLE_Trace, Trace.class.getName(),
				Trace.class, TraceFielder.class, router)));
		register(trace);
		
		thread = BaseDataRouter.cast(router.register(NodeFactory.create(clientName, 
				TABLE_TraceThread, TraceThread.class.getName(),
				TraceThread.class, TraceThreadFielder.class, router)));
		register(thread);
		
		span = BaseDataRouter.cast(router.register(NodeFactory.create(clientName, 
				TABLE_TraceSpan, TraceSpan.class.getName(),
				TraceSpan.class, TraceSpanFielder.class, router)));
		register(span);	
	}
	
	
	@Override
	public TraceEntity getEntity(TraceEntityKey key, Config pConfig){
		TraceEntity entity = new TraceEntity(key);
		
		TraceKey tracePrefix = ReflectionTool.create(TraceKey.class).prefixFromEntityKey(key);
		List<Trace> traces = trace.getWithPrefix(tracePrefix, false, null);
		entity.add(trace, traces);
		
		TraceThreadKey traceThreadPrefix = ReflectionTool.create(TraceThreadKey.class).prefixFromEntityKey(key);
		List<TraceThread> traceThreads = thread.getWithPrefix(traceThreadPrefix, false, null);
		entity.add(thread, traceThreads);
		
		TraceSpanKey traceSpanPrefix = ReflectionTool.create(TraceSpanKey.class).prefixFromEntityKey(key);
		List<TraceSpan> traceSpans = span.getWithPrefix(traceSpanPrefix, false, null);
		entity.add(span, traceSpans);
		
		return entity;
	}
	
	
	/*********************** get nodes ******************************/

	public SortedMapStorageNode<TraceKey,Trace> trace(){
		return trace;
	}

	public SortedMapStorageNode<TraceThreadKey,TraceThread> thread(){
		return thread;
	}

	public SortedMapStorageNode<TraceSpanKey,TraceSpan> span(){
		return span;
	}
	
	
}
