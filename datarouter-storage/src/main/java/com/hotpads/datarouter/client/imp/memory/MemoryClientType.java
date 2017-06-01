package com.hotpads.datarouter.client.imp.memory;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.ClientTypeRegistry;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.BaseClientType;
import com.hotpads.datarouter.client.imp.memory.node.MemoryNode;
import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.availability.PhysicalIndexedSortedMapStorageAvailabilityAdapter;
import com.hotpads.datarouter.node.adapter.callsite.physical.PhysicalIndexedSortedMapStorageCallsiteAdapter;
import com.hotpads.datarouter.node.adapter.counter.physical.PhysicalIndexedSortedMapStorageCounterAdapter;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;

@Singleton
public class MemoryClientType extends BaseClientType{

	private static final String NAME = "memory";

	public static MemoryClientType INSTANCE;

	private final ClientAvailabilitySettings clientAvailabilitySettings;

	@Inject
	public MemoryClientType(ClientAvailabilitySettings clientAvailabilitySettings){
		this.clientAvailabilitySettings = clientAvailabilitySettings;
		INSTANCE = this;
	}

	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public ClientFactory createClientFactory(DatarouterProperties datarouterProperties, Datarouter datarouter,
			String clientName){
		return new MemoryClientFactory(clientName, clientAvailabilitySettings);
	}

	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>, F extends DatabeanFielder<PK, D>>
	PhysicalNode<PK,D> createNode(NodeParams<PK, D, F> nodeParams){
		return new PhysicalIndexedSortedMapStorageAvailabilityAdapter<>(
				new PhysicalIndexedSortedMapStorageCounterAdapter<>(new MemoryNode<>(nodeParams)));
	}

	//ignore the entityNodeParams
	@Override
	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK, D>,
			F extends DatabeanFielder<PK, D>>
	Node<PK,D> createSubEntityNode(EntityNodeParams<EK,E> entityNodeParams, NodeParams<PK,D,F> nodeParams){
		return createNode(nodeParams);
	}


	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	MapStorageNode<PK,D> createAdapter(NodeParams<PK,D,F> nodeParams, Node<PK,D> backingNode){
		return new PhysicalIndexedSortedMapStorageCallsiteAdapter<>(nodeParams,
				(PhysicalIndexedSortedMapStorageNode<PK,D>) backingNode);
	}


	/********************** tests ****************************/

	@Guice(moduleFactory = DatarouterStorageTestModuleFactory.class)
	public static class MemoryClientTypeTests{
		@Inject
		private ClientTypeRegistry clientTypeRegistry;

		@Test
		public void testClassLocation(){
			Assert.assertEquals(clientTypeRegistry.create(NAME).getClass(), MemoryClientType.class);
		}
	}

}
