package com.hotpads.datarouter.node.factory;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.adapter.MapStorageAdapterNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.caching.map.MapCachingMapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.setting.DatarouterSettings;

@Singleton
public class CachingNodeFactory{
	private static final Logger logger = LoggerFactory.getLogger(CachingNodeFactory.class);
	
	private final DatarouterSettings drSettings;
	
	
	@Inject
	private CachingNodeFactory(DatarouterSettings drSettings){
		this.drSettings = drSettings;
	}


	/********************* pass any params *****************/
	
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends MapStorageNode<PK,D>> 
	MapStorageNode<PK,D> create(NodeParams<PK,D,F> params, 
			N cacheNode, 
			N backingNode, 
			boolean cacheReads, 
			boolean cacheWrites,
			boolean addAdapter){
		String clientName = params.getClientName();
		ClientType clientType = params.getRouter().getClientOptions().getClientTypeInstance(clientName);
		Preconditions.checkNotNull(clientType, "clientType not found for clientName:"+clientName);
		MapStorageNode<PK,D> node = new MapCachingMapStorageNode<PK,D,F,N>(cacheNode, backingNode, cacheReads,
				cacheWrites);
		if(addAdapter){
			node = new MapStorageAdapterNode<PK,D,F,N>(params, (N)node);
		}
		return Preconditions.checkNotNull(node, "cannot build Node for clientType="+clientType);
	}
	
	
	/*************** simple helpers *********************/
	
	// +fielderClass
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends MapStorageNode<PK,D>> 
	MapStorageNode<PK,D> create(
			Class<D> databeanClass, 
			DataRouter router,
			Class<F> fielderClass,
			N cacheNode, 
			N backingNode, 
			boolean cacheReads, 
			boolean cacheWrites,
			boolean addAdapter){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<PK,D,F>(router, databeanClass)
				.withFielder(fielderClass)
				.withDiagnostics(drSettings.getRecordCallsites());
		return create(paramsBuilder.build(), cacheNode, backingNode, cacheReads, cacheWrites, addAdapter);
	}
}
