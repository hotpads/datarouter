package com.hotpads.trace.node;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.entity.EntityNode;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.entity.SubEntitySortedMapStorageNode;
import com.hotpads.datarouter.node.factory.EntityNodeFactory;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.Router;
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
implements TraceSubNodes{

	public static final EntityNodeParams<TraceEntityKey,TraceEntity> ENTITY_NODE_PARAMS_TraceEntity16
			= new EntityNodeParams<>("TraceEntity16", TraceEntityKey.class, TraceEntity.class,
					TraceEntityPartitioner.class, "TraceEntity16");

	public static final EntityNodeParams<TraceEntityKey,TraceEntity> ENTITY_NODE_PARAMS_TraceEntityTest
			= new EntityNodeParams<>("TraceEntity", TraceEntityKey.class, TraceEntity.class,
					TraceEntityPartitioner.class, "TraceEntityTest");

	public EntityNode<TraceEntityKey,TraceEntity> entity;
	private SubEntitySortedMapStorageNode<TraceEntityKey,TraceKey,Trace,TraceFielder> trace;
	private SubEntitySortedMapStorageNode<TraceEntityKey,TraceThreadKey,TraceThread,TraceThreadFielder> thread;
	private SubEntitySortedMapStorageNode<TraceEntityKey,TraceSpanKey,TraceSpan,TraceSpanFielder> span;

	public TraceEntityNode(EntityNodeFactory entityNodeFactory, NodeFactory nodeFactory, Router router,
			ClientId clientId, EntityNodeParams<TraceEntityKey,TraceEntity> entityNodeParams){
		entity = entityNodeFactory.create(clientId.getName(), router, entityNodeParams);

		trace = router.register(nodeFactory.subEntityNode(router, entityNodeParams, clientId, Trace.class,
				TraceFielder.class, TraceEntity.QUALIFIER_PREFIX_Trace));
		entity.register(trace);

		thread = router.register(nodeFactory.subEntityNode(router, entityNodeParams, clientId, TraceThread.class,
				TraceThreadFielder.class, TraceEntity.QUALIFIER_PREFIX_TraceThread));
		entity.register(thread);

		span = router.register(nodeFactory.subEntityNode(router, entityNodeParams, clientId, TraceSpan.class,
				TraceSpanFielder.class, TraceEntity.QUALIFIER_PREFIX_TraceSpan));
		entity.register(span);
	}


	/*********************** get nodes ******************************/

	public EntityNode<TraceEntityKey,TraceEntity> entity(){
		return entity;
	}

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
