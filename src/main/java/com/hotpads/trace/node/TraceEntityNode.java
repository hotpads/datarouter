package com.hotpads.trace.node;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseEntityReaderNode;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTaskNameParams;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
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
import com.hotpads.trace.key.TraceEntityKey.TraceEntityPartitioner;
import com.hotpads.trace.key.TraceKey;
import com.hotpads.trace.key.TraceSpanKey;
import com.hotpads.trace.key.TraceThreadKey;

public class TraceEntityNode 
extends HBaseEntityReaderNode<TraceEntityKey,TraceEntity>
implements TraceNodes{
	
//	private static final String
//		NODE_NAME = "TraceEntity",
//		ENTITY_TABLE_NAME_TraceEntity = "TraceEntity";
	
	private static final EntityNodeParams<TraceEntityKey,TraceEntity> nodeParams
			= new EntityNodeParams<TraceEntityKey,TraceEntity>(
			"TraceEntity", TraceEntityKey.class, TraceEntity.class, TraceEntityPartitioner.class,
			"TraceEntity");

	private SubEntitySortedMapStorageNode<TraceEntityKey,TraceKey,Trace,TraceFielder> trace;
	private SubEntitySortedMapStorageNode<TraceEntityKey,TraceThreadKey,TraceThread,TraceThreadFielder> thread;
	private SubEntitySortedMapStorageNode<TraceEntityKey,TraceSpanKey,TraceSpan,TraceSpanFielder> span;
	
	public TraceEntityNode(DataRouter router, String clientName, String name){
		super(router, nodeParams, new HBaseTaskNameParams(clientName, nodeParams.getEntityTableName(), name));
	}
	
	
	@Override
	protected void initNodes(DataRouter router, String clientName){
		trace = BaseDataRouter.cast(router.register(NodeFactory.subEntityNode(router, nodeParams, clientName, 
				nodeParams.getNodeName(), Trace.class, TraceFielder.class, 
				nodeParams.getEntityTableName(), TraceEntity.QUALIFIER_PREFIX_Trace)));
		register(trace);
		
		thread = BaseDataRouter.cast(router.register(NodeFactory.subEntityNode(router, nodeParams, clientName, 
				nodeParams.getNodeName(), TraceThread.class, TraceThreadFielder.class, 
				nodeParams.getEntityTableName(), TraceEntity.QUALIFIER_PREFIX_TraceThread)));
		register(thread);
		
		span = BaseDataRouter.cast(router.register(NodeFactory.subEntityNode(router, nodeParams, clientName, 
				nodeParams.getNodeName(), TraceSpan.class, TraceSpanFielder.class, 
				nodeParams.getEntityTableName(), TraceEntity.QUALIFIER_PREFIX_TraceSpan)));
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
