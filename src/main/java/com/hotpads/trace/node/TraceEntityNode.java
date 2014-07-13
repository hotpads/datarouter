package com.hotpads.trace.node;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseEntityReaderNode;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTaskNameParams;
import com.hotpads.datarouter.node.entity.SubEntitySortedMapStorageNode;
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

public class TraceEntityNode 
extends HBaseEntityReaderNode<TraceEntityKey,TraceEntity>
implements TraceNodes{
	
	private static final String
		ENTITY_TraceEntity = "TestTraceEntity",
		NODE_PREFIX_Trace = "T",
		NODE_PREFIX_TraceThread = "TT",
		NODE_PREFIX_TraceSpan = "TS";

	public SubEntitySortedMapStorageNode<TraceEntityKey,TraceKey,Trace,TraceFielder> trace;
	public SubEntitySortedMapStorageNode<TraceEntityKey,TraceThreadKey,TraceThread,TraceThreadFielder> thread;
	public SubEntitySortedMapStorageNode<TraceEntityKey,TraceSpanKey,TraceSpan,TraceSpanFielder> span;
	
	public TraceEntityNode(DataRouter router, String clientName, String name){
		super(router, new HBaseTaskNameParams(clientName, ENTITY_TraceEntity, name));
	}
	
	
	@Override
	protected void initNodes(DataRouter router, String clientName){
		trace = BaseDataRouter.cast(router.register(NodeFactory.entityNode(router, clientName, 
				TraceEntityKey.class, Trace.class, TraceFielder.class, 
				ENTITY_TraceEntity, NODE_PREFIX_Trace)));
		register(trace);
		
		thread = BaseDataRouter.cast(router.register(NodeFactory.entityNode(router, clientName, 
				TraceEntityKey.class, TraceThread.class, TraceThreadFielder.class, 
				ENTITY_TraceEntity, NODE_PREFIX_TraceThread)));
		register(thread);
		
		span = BaseDataRouter.cast(router.register(NodeFactory.entityNode(router, clientName, 
				TraceEntityKey.class, TraceSpan.class, TraceSpanFielder.class, 
				ENTITY_TraceEntity, NODE_PREFIX_TraceSpan)));
		register(span);	
	}
	
//	@Override
//	public TraceEntity getEntity(TraceEntityKey key, Config pConfig){
//		final Config config = Config.nullSafe(pConfig);
//		return new HBaseMultiAttemptTask<TraceEntity>(new HBaseTask<TraceEntity>(getContext(), getTaskNameParams(), 
//				"getEntity", config){
//				public E hbaseCall() throws Exception{
//					byte[] rowBytes = queryBuilder.getRowBytes(pk.getEntityKey());
//					Get get = new Get(rowBytes);
//					Result hBaseResult = hTable.get(get);
//					return resultParser.getDatabeansWithMatchingQualifierPrefix(rows);
//				}
//			}).call();
//	}
	
	
//	@Override
//	public TraceEntity getEntity(TraceEntityKey key){
//		TraceEntity entity = new TraceEntity(key);
//		
//		TraceKey tracePrefix = ReflectionTool.create(TraceKey.class).prefixFromEntityKey(key);
//		List<Trace> traces = trace.getWithPrefix(tracePrefix, false, null);
//		entity.add(trace, traces);
//		
//		TraceThreadKey traceThreadPrefix = ReflectionTool.create(TraceThreadKey.class).prefixFromEntityKey(key);
//		List<TraceThread> traceThreads = thread.getWithPrefix(traceThreadPrefix, false, null);
//		entity.add(thread, traceThreads);
//		
//		TraceSpanKey traceSpanPrefix = ReflectionTool.create(TraceSpanKey.class).prefixFromEntityKey(key);
//		List<TraceSpan> traceSpans = span.getWithPrefix(traceSpanPrefix, false, null);
//		entity.add(span, traceSpans);
//		
//		return entity;
//	}
	
	
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
