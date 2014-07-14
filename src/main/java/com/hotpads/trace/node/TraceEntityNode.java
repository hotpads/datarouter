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
		NODE_NAME = "TraceEntity",
		ENTITY_TABLE_NAME_TraceEntity = "TraceEntity";

	private SubEntitySortedMapStorageNode<TraceEntityKey,TraceKey,Trace,TraceFielder> trace;
	private SubEntitySortedMapStorageNode<TraceEntityKey,TraceThreadKey,TraceThread,TraceThreadFielder> thread;
	private SubEntitySortedMapStorageNode<TraceEntityKey,TraceSpanKey,TraceSpan,TraceSpanFielder> span;
	
	public TraceEntityNode(DataRouter router, String clientName, String name){
		super(router, new HBaseTaskNameParams(clientName, ENTITY_TABLE_NAME_TraceEntity, name));
	}
	
	
	@Override
	protected void initNodes(DataRouter router, String clientName){
		trace = BaseDataRouter.cast(router.register(NodeFactory.subEntityNode(router, clientName, NODE_NAME,
				TraceEntityKey.class, Trace.class, TraceFielder.class, 
				TraceEntity.class, ENTITY_TABLE_NAME_TraceEntity, TraceEntity.QUALIFIER_PREFIX_Trace)));
		register(trace);
		
		thread = BaseDataRouter.cast(router.register(NodeFactory.subEntityNode(router, clientName, NODE_NAME,
				TraceEntityKey.class, TraceThread.class, TraceThreadFielder.class, 
				TraceEntity.class, ENTITY_TABLE_NAME_TraceEntity, TraceEntity.QUALIFIER_PREFIX_TraceThread)));
		register(thread);
		
		span = BaseDataRouter.cast(router.register(NodeFactory.subEntityNode(router, clientName, NODE_NAME,
				TraceEntityKey.class, TraceSpan.class, TraceSpanFielder.class, 
				TraceEntity.class, ENTITY_TABLE_NAME_TraceEntity, TraceEntity.QUALIFIER_PREFIX_TraceSpan)));
		register(span);	
	}
	
	
	/*********************** get nodes ******************************/

	@Override
	public SortedMapStorageNode<TraceKey,Trace> trace(){
		return trace;
	}

	@Override
	public SortedMapStorageNode<TraceThreadKey,TraceThread> thread(){
		return thread;
	}

	@Override
	public SortedMapStorageNode<TraceSpanKey,TraceSpan> span(){
		return span;
	}
	
	
}
