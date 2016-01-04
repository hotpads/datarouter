package com.hotpads.trace.node;

import java.util.List;
import java.util.stream.Collectors;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.entity.BaseEntityNode;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.routing.Router;
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

//not currently needed, but a example of an entity built from separate tables
public class TraceCompoundNode
extends BaseEntityNode<TraceEntityKey,TraceEntity>
implements TraceSubNodes{

	private NodeFactory nodeFactory;

	public SortedMapStorageNode<TraceKey,Trace> trace;
	public SortedMapStorageNode<TraceThreadKey,TraceThread> thread;
	public SortedMapStorageNode<TraceSpanKey,TraceSpan> span;

	public TraceCompoundNode(NodeFactory nodeFactory, Router router, Datarouter datarouter,
			ClientId clientId, String name){
		super(datarouter, name);
		this.nodeFactory = nodeFactory;
		initNodes(router, clientId);
	}

	private void initNodes(Router router, ClientId clientId){
		trace = router.register(nodeFactory.create(clientId, Trace.class, TraceFielder.class, router, true));
		register(trace);

		thread = router.register(nodeFactory.create(clientId, TraceThread.class, TraceThreadFielder.class, router,
				true));
		register(thread);

		span = router.register(nodeFactory.create(clientId, TraceSpan.class, TraceSpanFielder.class, router, true));
		register(span);
	}


	@Override
	public TraceEntity getEntity(TraceEntityKey key, Config config){
		TraceEntity entity = new TraceEntity(key);

		TraceKey tracePrefix = ReflectionTool.create(TraceKey.class).prefixFromEntityKey(key);
		List<Trace> traces = trace.streamWithPrefix(tracePrefix, null).collect(Collectors.toList());
		entity.addDatabeansForQualifierPrefix(TraceEntity.QUALIFIER_PREFIX_Trace, traces);

		TraceThreadKey traceThreadPrefix = ReflectionTool.create(TraceThreadKey.class).prefixFromEntityKey(key);
		List<TraceThread> traceThreads = thread.streamWithPrefix(traceThreadPrefix, null).collect(Collectors.toList());
		entity.addDatabeansForQualifierPrefix(TraceEntity.QUALIFIER_PREFIX_TraceThread, traceThreads);

		TraceSpanKey traceSpanPrefix = ReflectionTool.create(TraceSpanKey.class).prefixFromEntityKey(key);
		List<TraceSpan> traceSpans = span.streamWithPrefix(traceSpanPrefix, null).collect(Collectors.toList());
		entity.addDatabeansForQualifierPrefix(TraceEntity.QUALIFIER_PREFIX_TraceSpan, traceSpans);

		return entity;
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
