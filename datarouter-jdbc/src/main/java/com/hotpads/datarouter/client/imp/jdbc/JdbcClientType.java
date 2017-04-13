package com.hotpads.datarouter.client.imp.jdbc;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.DefaultClientTypes;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.BaseClientType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.execute.JdbcSchemaUpdateService.JdbcSchemaUpdateServiceFactory;
import com.hotpads.datarouter.client.imp.jdbc.factory.JdbcSimpleClientFactory;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.availability.PhysicalIndexedSortedMapStorageAvailabilityAdapter;
import com.hotpads.datarouter.node.adapter.callsite.physical.PhysicalIndexedSortedMapStorageCallsiteAdapter;
import com.hotpads.datarouter.node.adapter.counter.physical.PhysicalIndexedSortedMapStorageCounterAdapter;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.lang.ClassTool;

@Singleton
public class JdbcClientType extends BaseClientType{

	public static JdbcClientType INSTANCE;//TODO get rid of

	//injected
	private final DatarouterProperties datarouterProperties;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final ClientAvailabilitySettings clientAvailabilitySettings;
	private final JdbcSchemaUpdateServiceFactory schemaUpdateServiceFactory;

	@Inject
	public JdbcClientType(DatarouterProperties datarouterProperties, JdbcFieldCodecFactory fieldCodecFactory,
			ClientAvailabilitySettings clientAvailabilitySettings,
			JdbcSchemaUpdateServiceFactory schemaUpdateServiceFactory){
		this.datarouterProperties = datarouterProperties;
		this.fieldCodecFactory = fieldCodecFactory;
		this.clientAvailabilitySettings = clientAvailabilitySettings;
		this.schemaUpdateServiceFactory = schemaUpdateServiceFactory;
		INSTANCE = this;
	}

	@Override
	public String getName(){
		return DefaultClientTypes.CLIENT_TYPE_jdbc;
	}

	@Override
	public ClientFactory createClientFactory(Datarouter datarouter, String clientName){
		return new JdbcSimpleClientFactory(datarouterProperties, datarouter, clientName, clientAvailabilitySettings,
				schemaUpdateServiceFactory);
	}

	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>, F extends DatabeanFielder<PK, D>>
	PhysicalNode<PK, D> createNode(NodeParams<PK, D, F> nodeParams){
		return new PhysicalIndexedSortedMapStorageAvailabilityAdapter<>(
				new PhysicalIndexedSortedMapStorageCounterAdapter<>(new JdbcNode<>(nodeParams, fieldCodecFactory)));
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
	IndexedSortedMapStorageNode<PK,D> createAdapter(NodeParams<PK,D,F> nodeParams, Node<PK,D> backingNode){
		return new PhysicalIndexedSortedMapStorageCallsiteAdapter<>(
				nodeParams, (PhysicalIndexedSortedMapStorageNode<PK,D>)backingNode);
	}

	/********************** tests ****************************/

	@Guice(moduleFactory = TestDatarouterJdbcModuleFactory.class)
	public static class JdbcClientTypeIntegrationTests{
		@Inject
		private DatarouterInjector injector;

		@Test
		public void testClassLocation(){
			String actualClassName = JdbcClientType.class.getCanonicalName();
			Assert.assertEquals(DefaultClientTypes.CLIENT_CLASS_jdbc, actualClassName);
			injector.getInstance(ClassTool.forName(DefaultClientTypes.CLIENT_CLASS_jdbc));
		}
	}

}
