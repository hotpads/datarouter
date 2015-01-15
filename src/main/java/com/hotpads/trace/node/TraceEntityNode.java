package com.hotpads.trace.node;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseEntityReaderNode;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTaskNameParams;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.entity.SubEntitySortedMapStorageNode;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseDatarouter;
import com.hotpads.datarouter.routing.Datarouter;
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
	
	public static final EntityNodeParams<TraceEntityKey,TraceEntity> ENTITY_NODE_PARAMS_TraceEntity
			= new EntityNodeParams<TraceEntityKey,TraceEntity>(
			"TraceEntity", TraceEntityKey.class, TraceEntity.class, TraceEntityPartitioner.class,
			"TraceEntity");
	
	public static final EntityNodeParams<TraceEntityKey,TraceEntity> ENTITY_NODE_PARAMS_TraceEntityTest
			= new EntityNodeParams<TraceEntityKey,TraceEntity>(
			"TraceEntity", TraceEntityKey.class, TraceEntity.class, TraceEntityPartitioner.class,
			"TraceEntityTest");
	

	private SubEntitySortedMapStorageNode<TraceEntityKey,TraceKey,Trace,TraceFielder> trace;
	private SubEntitySortedMapStorageNode<TraceEntityKey,TraceThreadKey,TraceThread,TraceThreadFielder> thread;
	private SubEntitySortedMapStorageNode<TraceEntityKey,TraceSpanKey,TraceSpan,TraceSpanFielder> span;
	
	public TraceEntityNode(NodeFactory nodeFactory, Datarouter router, String clientName, 
			EntityNodeParams<TraceEntityKey,TraceEntity> entityNodeParams){
		super(nodeFactory, router, entityNodeParams, new HBaseTaskNameParams(clientName, 
				entityNodeParams.getEntityTableName(), entityNodeParams.getNodeName()));
	}
	
	
	@Override
	protected void initNodes(Datarouter router, String clientName){
		trace = BaseDatarouter.cast(router.register(nodeFactory.subEntityNode(router, entityNodeParams, clientName, 
				Trace.class, TraceFielder.class, TraceEntity.QUALIFIER_PREFIX_Trace)));
		register(trace);
		
		thread = BaseDatarouter.cast(router.register(nodeFactory.subEntityNode(router, entityNodeParams, clientName, 
				TraceThread.class, TraceThreadFielder.class, TraceEntity.QUALIFIER_PREFIX_TraceThread)));
		register(thread);
		
		span = BaseDatarouter.cast(router.register(nodeFactory.subEntityNode(router, entityNodeParams, clientName, 
				TraceSpan.class, TraceSpanFielder.class, TraceEntity.QUALIFIER_PREFIX_TraceSpan)));
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
