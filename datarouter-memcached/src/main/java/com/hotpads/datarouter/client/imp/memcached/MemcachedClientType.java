package com.hotpads.datarouter.client.imp.memcached;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.DefaultClientTypes;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.BaseClientType;
import com.hotpads.datarouter.client.imp.memcached.client.MemcachedSimpleClientFactory;
import com.hotpads.datarouter.client.imp.memcached.node.MemcachedNode;
import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.availability.PhysicalMapStorageAvailabilityAdapter;
import com.hotpads.datarouter.node.adapter.callsite.physical.PhysicalMapStorageCallsiteAdapter;
import com.hotpads.datarouter.node.adapter.counter.physical.PhysicalMapStorageCounterAdapter;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
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

/**
 * applications create this class via reflection
 */
@Singleton
public class MemcachedClientType extends BaseClientType{

	public static MemcachedClientType INSTANCE;
	private final ClientAvailabilitySettings clientAvailabilitySettings;

	@Inject
	public MemcachedClientType(ClientAvailabilitySettings clientAvailabilitySettings){
		this.clientAvailabilitySettings = clientAvailabilitySettings;
		INSTANCE = this;
	}

	@Override
	public String getName(){
		return DefaultClientTypes.CLIENT_TYPE_memcached;
	}

	@Override
	public ClientFactory createClientFactory(Datarouter datarouter, String clientName){
		return new MemcachedSimpleClientFactory(datarouter, clientName, clientAvailabilitySettings);
	}

	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>, F extends DatabeanFielder<PK, D>>
	PhysicalNode<PK, D> createNode(NodeParams<PK, D, F> nodeParams){
		return new PhysicalMapStorageAvailabilityAdapter<>(new PhysicalMapStorageCounterAdapter<>(new MemcachedNode<>(
				nodeParams)));
	}

	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>, F extends DatabeanFielder<PK, D>>
	MemcachedNode<PK,D,F> createNodeWithoutAdapters(NodeParams<PK, D, F> nodeParams){
		return new MemcachedNode<>(nodeParams);
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
		return new PhysicalMapStorageCallsiteAdapter<>(nodeParams, (PhysicalMapStorageNode<PK,D>) backingNode);
	}


	/********************** tests ****************************/

	@Guice(moduleFactory = DatarouterStorageTestModuleFactory.class)
	public static class MemcachedClientTypeTests{
		@Inject
		private DatarouterInjector injector;

		@Test
		public void testClassLocation(){
			String actualClassName = MemcachedClientType.class.getCanonicalName();
			Assert.assertEquals(actualClassName, DefaultClientTypes.CLIENT_CLASS_memcached);
			injector.getInstance(ClassTool.forName(DefaultClientTypes.CLIENT_CLASS_memcached));
		}
	}

}
