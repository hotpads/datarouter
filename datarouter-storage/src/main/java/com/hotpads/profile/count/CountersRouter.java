package com.hotpads.profile.count;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.profile.count.collection.archive.CountPartitionedNode;
import com.hotpads.profile.count.databean.AvailableCounter;
import com.hotpads.profile.count.databean.AvailableCounter.AvailableCounterFielder;
import com.hotpads.profile.count.databean.Count;
import com.hotpads.profile.count.databean.key.AvailableCounterKey;
import com.hotpads.profile.count.databean.key.CountKey;

public class CountersRouter extends BaseRouter implements CountersNodes {

	private static final String NAME = "counters";

	private static final ClientId CLIENT_hbase1 = new ClientId("hbase1", true);

	public static final String
		TABLE_AvailableCounter2 = AvailableCounter.class.getSimpleName()+"2",
		ENTITY_AvailableCounter2 = AvailableCounter.class.getCanonicalName()+"2";

	private final NodeFactory nodeFactory;

	@Inject
	public CountersRouter(String configLocation, Datarouter context, NodeFactory nodeFactory) {
		super(context, configLocation, NAME);
		this.nodeFactory = nodeFactory;
		createNodes();
	}

	@Override
	public List<ClientId> getClientIds() {
		return Collections.singletonList(CLIENT_hbase1);
	}

	public SortedMapStorageNode<AvailableCounterKey,AvailableCounter> availableCounter;

	public CountPartitionedNode count;

	private void createNodes() {
		availableCounter = register(nodeFactory.create(CLIENT_hbase1, TABLE_AvailableCounter2,
				ENTITY_AvailableCounter2, AvailableCounter.class, AvailableCounterFielder.class, this, true));
		count = register(new CountPartitionedNode(nodeFactory, this, CLIENT_hbase1));
	}

	@Override
	public SortedMapStorageNode<AvailableCounterKey, AvailableCounter> getAvailableCounter() {
		return availableCounter;
	}

	@Override
	public SortedMapStorageNode<CountKey,Count> getCount() {
		return count;
	}
}
