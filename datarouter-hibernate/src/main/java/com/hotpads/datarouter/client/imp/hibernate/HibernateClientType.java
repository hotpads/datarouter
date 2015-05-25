package com.hotpads.datarouter.client.imp.hibernate;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.DatarouterInjector;
import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.imp.BaseClientType;
import com.hotpads.datarouter.client.imp.hibernate.client.HibernateSimpleClientFactory;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.client.imp.hibernate.util.HibernateResultParser;
import com.hotpads.datarouter.client.imp.jdbc.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.callsite.physical.PhysicalIndexedSortedMapStorageCallsiteAdapter;
import com.hotpads.datarouter.node.adapter.counter.physical.PhysicalIndexedSortedMapStorageCounterAdapter;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.lang.ClassTool;

@Singleton
public class HibernateClientType extends BaseClientType{
	
	public static final String 
			NAME = "hibernate",
			CANONICAL_CLASS_NAME = "com.hotpads.datarouter.client.imp.hibernate.HibernateClientType";
	
	public static HibernateClientType INSTANCE;
	
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final HibernateResultParser resultParser;
	
	@Inject
	public HibernateClientType(JdbcFieldCodecFactory fieldCodecFactory){
		this.fieldCodecFactory = fieldCodecFactory;
		this.resultParser = new HibernateResultParser(fieldCodecFactory);
		INSTANCE = this;
	}
	
	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public ClientFactory createClientFactory(DatarouterContext drContext, String clientName,
			List<PhysicalNode<?,?>> physicalNodes){
		return new HibernateSimpleClientFactory(drContext, fieldCodecFactory, clientName); 
	}
	
	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>, F extends DatabeanFielder<PK, D>>
	PhysicalNode<PK, D> createNode(NodeParams<PK, D, F> nodeParams){
		PhysicalIndexedSortedMapStorageNode<PK,D> node;
		if(nodeParams.getFielderClass() == null){
			node = new PhysicalIndexedSortedMapStorageCounterAdapter<PK,D,F,HibernateNode<PK,D,F>>(
					new HibernateNode<>(nodeParams, fieldCodecFactory, resultParser));
//			logger.warn("creating HibernateNode "+node);
		}else{
			node = new PhysicalIndexedSortedMapStorageCounterAdapter<PK,D,F,JdbcNode<PK,D,F>>(
					new JdbcNode<>(nodeParams, fieldCodecFactory));
		}
		return node;
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
		return new PhysicalIndexedSortedMapStorageCallsiteAdapter<>(nodeParams,
				(PhysicalIndexedSortedMapStorageNode<PK,D>)backingNode);
	}
	
	/********************** tests ****************************/
	
	@Guice(moduleFactory = TestDatarouterJdbcModuleFactory.class)
	public static class HibernateClientTypeIntegrationTests{
		@Inject
		private DatarouterInjector injector;
		
		@Test
		public void testClassLocation(){
			String actualClassName = HibernateClientType.class.getCanonicalName();
			Assert.assertEquals(CANONICAL_CLASS_NAME, actualClassName);
			injector.getInstance(ClassTool.forName(CANONICAL_CLASS_NAME));
		}
	}
	
}
