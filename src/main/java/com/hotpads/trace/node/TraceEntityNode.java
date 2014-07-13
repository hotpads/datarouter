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

	private SubEntitySortedMapStorageNode<TraceEntityKey,TraceKey,Trace,TraceFielder> trace;
	private SubEntitySortedMapStorageNode<TraceEntityKey,TraceThreadKey,TraceThread,TraceThreadFielder> thread;
	private SubEntitySortedMapStorageNode<TraceEntityKey,TraceSpanKey,TraceSpan,TraceSpanFielder> span;
	
	public TraceEntityNode(DataRouter router, String clientName, String name){
		super(router, new HBaseTaskNameParams(clientName, ENTITY_TraceEntity, name));
	}
	
	
	@Override
	protected void initNodes(DataRouter router, String clientName){
		trace = BaseDataRouter.cast(router.register(NodeFactory.entityNode(router, clientName, 
				TraceEntityKey.class, Trace.class, TraceFielder.class, 
				TraceEntity.class, ENTITY_TraceEntity, NODE_PREFIX_Trace)));
		register(trace);
		
		thread = BaseDataRouter.cast(router.register(NodeFactory.entityNode(router, clientName, 
				TraceEntityKey.class, TraceThread.class, TraceThreadFielder.class, 
				TraceEntity.class, ENTITY_TraceEntity, NODE_PREFIX_TraceThread)));
		register(thread);
		
		span = BaseDataRouter.cast(router.register(NodeFactory.entityNode(router, clientName, 
				TraceEntityKey.class, TraceSpan.class, TraceSpanFielder.class, 
				TraceEntity.class, ENTITY_TraceEntity, NODE_PREFIX_TraceSpan)));
		register(span);	
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
