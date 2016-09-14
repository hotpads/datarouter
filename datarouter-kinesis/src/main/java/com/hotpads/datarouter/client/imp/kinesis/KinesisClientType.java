package com.hotpads.datarouter.client.imp.kinesis;

import javax.inject.Inject;

import org.junit.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.DefaultClientTypes;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.BaseClientType;
import com.hotpads.datarouter.client.imp.StreamClientType;
import com.hotpads.datarouter.client.imp.kinesis.single.KinesisNode;
import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
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

public class KinesisClientType extends BaseClientType implements StreamClientType{

	@Inject
	private KinesisNodeFactory kinesisNodeFactory;
	@Inject
	private ClientAvailabilitySettings clientAvailabilitySettings;


	@Override
	public String getName(){
		return DefaultClientTypes.CLIENT_TYPE_kinesis;
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK, D>,
			F extends DatabeanFielder<PK, D>>
	PhysicalNode<PK, D> createNode(NodeParams<PK, D, F> nodeParams){
		return createSingleStreamNode(nodeParams);
	}

	@Override
	public ClientFactory createClientFactory(Datarouter datarouter, String clientName){
		KinesisOptions kinesisOptions = new KinesisOptions(datarouter, clientName);
		return new KinesisClientFactory(clientName, this, kinesisOptions, clientAvailabilitySettings);
	}

	@Override
	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK, PK>,
			D extends Databean<PK, D>,
			F extends DatabeanFielder<PK, D>>
	Node<PK, D> createSubEntityNode(EntityNodeParams<EK, E> entityNodeParams, NodeParams<PK, D, F> nodeParams){
		return createNode(nodeParams);
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK, D>,
			F extends DatabeanFielder<PK, D>>
	Node<PK, D> createAdapter(NodeParams<PK, D, F> nodeParams, Node<PK, D> backingNode){
		return backingNode;
	}

	@Override
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> PhysicalNode<PK,D>
			createSingleStreamNode(NodeParams<PK,D,F> nodeParams){
		KinesisNode<PK,D,F> node = kinesisNodeFactory.createSingleNode(nodeParams);
		return node;
	}

	/********************** tests ****************************/

	@Guice(moduleFactory = DatarouterStorageTestModuleFactory.class)
	public static class KinesisClientTypeTests{
		@Inject
		private DatarouterInjector injector;

		@Test
		public void testClassLocation(){
			String actualClassName = KinesisClientType.class.getCanonicalName();
			Assert.assertEquals(DefaultClientTypes.CLIENT_CLASS_kinesis, actualClassName);
			injector.getInstance(ClassTool.forName(DefaultClientTypes.CLIENT_CLASS_kinesis));
		}
	}

}
