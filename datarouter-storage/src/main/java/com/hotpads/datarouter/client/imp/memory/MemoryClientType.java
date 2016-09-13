package com.hotpads.datarouter.client.imp.memory;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.DefaultClientTypes;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.BaseClientType;
import com.hotpads.datarouter.client.imp.memory.node.MemoryNode;
import com.hotpads.datarouter.inject.DatarouterInjector;
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
import com.hotpads.util.core.lang.ClassTool;

@Singleton
public class MemoryClientType extends BaseClientType{

	public static MemoryClientType INSTANCE;

	private final ClientAvailabilitySettings clientAvailabilitySettings;

	@Inject
	public MemoryClientType(ClientAvailabilitySettings clientAvailabilitySettings){
		this.clientAvailabilitySettings = clientAvailabilitySettings;
		INSTANCE = this;
	}

	@Override
	public String getName(){
		return DefaultClientTypes.CLIENT_TYPE_memory;
	}

	@Override
	public ClientFactory createClientFactory(Datarouter datarouter, String clientName){
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
		private DatarouterInjector injector;

		@Test
		public void testClassLocation(){
			String actualClassName = MemoryClientType.class.getCanonicalName();
			Assert.assertEquals(DefaultClientTypes.CLIENT_CLASS_memory, actualClassName);
			injector.getInstance(ClassTool.forName(DefaultClientTypes.CLIENT_CLASS_memory));
		}
	}

}
