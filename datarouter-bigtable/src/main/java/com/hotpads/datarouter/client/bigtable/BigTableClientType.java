package com.hotpads.datarouter.client.bigtable;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.junit.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.ClientTableNodeNames;
import com.hotpads.datarouter.client.DefaultClientTypes;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.bigtable.client.BigTableClientFactory;
import com.hotpads.datarouter.client.imp.BaseClientType;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseEntityReaderNode;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseNode;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityNode;
import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.inject.guice.executor.DatarouterExecutorGuiceModule;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.availability.PhysicalSortedMapStorageAvailabilityAdapter;
import com.hotpads.datarouter.node.adapter.callsite.physical.PhysicalSortedMapStorageCallsiteAdapter;
import com.hotpads.datarouter.node.adapter.counter.physical.PhysicalSortedMapStorageCounterAdapter;
import com.hotpads.datarouter.node.entity.EntityNode;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;
import com.hotpads.util.core.lang.ClassTool;

@Singleton
public class BigTableClientType extends BaseClientType{

	public static BigTableClientType INSTANCE;

	private final ClientAvailabilitySettings clientAvailabilitySettings;
	private final ExecutorService executor;

	@Inject
	public BigTableClientType(ClientAvailabilitySettings clientAvailabilitySettings,
			@Named(DatarouterExecutorGuiceModule.POOL_hbaseClientExecutor) ExecutorService executor){
		this.clientAvailabilitySettings = clientAvailabilitySettings;
		this.executor = executor;
		INSTANCE = this;
	}

	@Override
	public String getName(){
		return DefaultClientTypes.CLIENT_TYPE_bigtable;
	}

	@Override
	public ClientFactory createClientFactory(Datarouter datarouter, String clientName){
		return new BigTableClientFactory(datarouter, clientName, clientAvailabilitySettings, executor);
	}

	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>, F extends DatabeanFielder<PK, D>>
	PhysicalNode<PK, D> createNode(NodeParams<PK, D, F> nodeParams){
		return new PhysicalSortedMapStorageAvailabilityAdapter<>(new PhysicalSortedMapStorageCounterAdapter<>(
				new HBaseNode<>(nodeParams)));
	}

	@Override
	public <EK extends EntityKey<EK>,E extends Entity<EK>>EntityNode<EK,E> createEntityNode(NodeFactory nodeFactory,
			Router router, EntityNodeParams<EK,E> entityNodeParams, String clientName){
		ClientTableNodeNames clientTableNodeNames = new ClientTableNodeNames(clientName,
				entityNodeParams.getEntityTableName(), entityNodeParams.getNodeName());
		return new HBaseEntityReaderNode<>(nodeFactory, router, entityNodeParams, clientTableNodeNames);
	}

	@Override
	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK, D>,
			F extends DatabeanFielder<PK, D>>
	Node<PK,D> createSubEntityNode(EntityNodeParams<EK,E> entityNodeParams, NodeParams<PK,D,F> nodeParams){
		return new HBaseSubEntityNode<>(entityNodeParams, nodeParams);
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	SortedMapStorageNode<PK,D> createAdapter(NodeParams<PK,D,F> nodeParams, Node<PK,D> backingNode){
		return new PhysicalSortedMapStorageCallsiteAdapter<>(nodeParams,
				(PhysicalSortedMapStorageNode<PK, D>) backingNode);
	}


	/********************** tests ****************************/

	@Guice(moduleFactory = DatarouterStorageTestModuleFactory.class)
	public static class HBaseClientTypeTests{
		@Inject
		private DatarouterInjector injector;

		@Test
		public void testClassLocation(){
			String actualClassName = BigTableClientType.class.getCanonicalName();
			Assert.assertEquals(DefaultClientTypes.CLIENT_CLASS_bigtable, actualClassName);
			injector.getInstance(ClassTool.forName(DefaultClientTypes.CLIENT_CLASS_bigtable));
		}
	}
}