package com.hotpads.datarouter.client.imp.memcached;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.google.inject.Injector;
import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.imp.BaseClientType;
import com.hotpads.datarouter.client.imp.memcached.client.MemcachedSimpleClientFactory;
import com.hotpads.datarouter.client.imp.memcached.node.MemcachedNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.callsite.physical.PhysicalMapStorageCallsiteAdapter;
import com.hotpads.datarouter.node.adapter.counter.physical.PhysicalMapStorageCounterAdapter;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.test.DatarouterTestModuleFactory;
import com.hotpads.datarouter.util.core.DrClassTool;

/**
 * applications create this class via reflection
 */
@Singleton
public class MemcachedClientType extends BaseClientType{
	
	public static final String 
			NAME = "memcached",
			CANONICAL_CLASS_NAME = "com.hotpads.datarouter.client.imp.memcached.MemcachedClientType";
	
	public static final MemcachedClientType INSTANCE = new MemcachedClientType();
	
	
	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public ClientFactory createClientFactory(DatarouterContext drContext, String clientName,
			List<PhysicalNode<?,?>> physicalNodes){
		return new MemcachedSimpleClientFactory(drContext, clientName);
	}
	
	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>, F extends DatabeanFielder<PK, D>>
	PhysicalNode<PK, D> createNode(NodeParams<PK, D, F> nodeParams){
		return new PhysicalMapStorageCounterAdapter<PK,D,F,MemcachedNode<PK,D,F>>(new MemcachedNode<PK,D,F>(nodeParams));
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
		return new PhysicalMapStorageCallsiteAdapter<PK,D,F,PhysicalMapStorageNode<PK,D>>(nodeParams,
				(PhysicalMapStorageNode<PK,D>)backingNode);
	}
	
	
	/********************** tests ****************************/

	@Guice(moduleFactory = DatarouterTestModuleFactory.class)
	public static class MemcachedClientTypeTests{
		@Inject
		private Injector injector;
		
		@Test
		public void testClassLocation(){
			String actualClassName = MemcachedClientType.class.getCanonicalName();
			Assert.assertEquals(CANONICAL_CLASS_NAME, actualClassName);
			injector.getInstance(DrClassTool.forName(CANONICAL_CLASS_NAME));
		}
	}
	
}
