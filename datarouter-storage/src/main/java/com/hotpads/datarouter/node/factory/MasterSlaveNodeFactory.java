package com.hotpads.datarouter.node.factory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.adapter.callsite.IndexedSortedMapStorageCallsiteAdapter;
import com.hotpads.datarouter.node.adapter.callsite.MapStorageCallsiteAdapter;
import com.hotpads.datarouter.node.adapter.callsite.SortedMapStorageCallsiteAdapter;
import com.hotpads.datarouter.node.adapter.counter.MapStorageCounterAdapter;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveIndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveMapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveSortedMapStorageNode;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.StorageType;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.setting.DatarouterSettings;

@Singleton
public class MasterSlaveNodeFactory{

	private final DatarouterSettings drSettings;
	private final NodeFactory nodeFactory;

	@Inject
	public MasterSlaveNodeFactory(DatarouterSettings drSettings, NodeFactory nodeFactory){
		this.drSettings = drSettings;
		this.nodeFactory = nodeFactory;
	}


	//no Fielder
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	Node<PK,D>
	create(Router router, StorageType storageType, Class<D> databeanClass, ClientId masterClientId,
			Collection<ClientId> slaveClientIds){
		return createInternal(router, storageType, databeanClass, null, masterClientId, slaveClientIds);
	}


	/************** convenience methods that (try to) cast to the desired type *****************/

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends BaseNode<PK,D,F>>
	MapStorageNode<PK,D> createMap(Router router, Class<D> databeanClass,
			Class<F> fielderClass, ClientId masterClientId, Collection<ClientId> slaveClientIds){
		return (MapStorageNode<PK,D>)createInternal(router, StorageType.map, databeanClass,
				fielderClass, masterClientId, slaveClientIds);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends BaseNode<PK,D,F>>
	SortedMapStorageNode<PK,D> createSorted(Router router, Class<D> databeanClass,
			Class<F> fielderClass, ClientId masterClientId, Collection<ClientId> slaveClientIds){
		return (SortedMapStorageNode<PK,D>)createInternal(router, StorageType.sortedMap, databeanClass,
				fielderClass, masterClientId, slaveClientIds);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends BaseNode<PK,D,F>>
	IndexedSortedMapStorageNode<PK,D> createIndexed(Router router, Class<D> databeanClass,
			Class<F> fielderClass, ClientId masterClientId, Collection<ClientId> slaveClientIds){
		return (IndexedSortedMapStorageNode<PK,D>)createInternal(router, StorageType.indexed,
				databeanClass, fielderClass, masterClientId, slaveClientIds);
	}


	/************** private ******************/

	private <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>>
	Node<PK,D> createInternal(Router router, StorageType storageType, Class<D> databeanClass,
			Class<F> fielderClass, ClientId masterClientId, Collection<ClientId> slaveClientIds){

		//create the backing nodes
		N master = null;
		if(masterClientId != null){
			master = nodeFactory.create(masterClientId, databeanClass, fielderClass, router, false);
		}

		List<N> slaves = new LinkedList<>();
		for(ClientId slaveClientId : DrCollectionTool.nullSafe(slaveClientIds)){
			N slaveNode = nodeFactory.create(slaveClientId, databeanClass, fielderClass, router, false);
			slaves.add(slaveNode);
		}

		NodeParams<PK,D,F> nodeParams = new NodeParamsBuilder<PK,D,F>(router, databeanClass)
				.withFielder(fielderClass)
				.withDiagnostics(drSettings.getRecordCallsites())
				.build();

		//create the parent node.  cast the master/slave nodes so we throw an error here if they aren't the right type
		if(StorageType.map == storageType){
			MasterSlaveMapStorageNode<PK,D,F,MapStorageNode<PK,D>> backingNode
					= new MasterSlaveMapStorageNode<>(
					databeanClass, router,
					(MapStorageNode<PK,D>)master, (List<MapStorageNode<PK,D>>)slaves);
			MapStorageCounterAdapter counterAdapter = new MapStorageCounterAdapter<>(backingNode);
			return new MapStorageCallsiteAdapter<>(nodeParams, counterAdapter);
		}else if(StorageType.sortedMap == storageType){
			MasterSlaveSortedMapStorageNode<PK,D,F,SortedMapStorageNode<PK,D>> backingNode
					= new MasterSlaveSortedMapStorageNode<>(
					databeanClass, router,
					(SortedMapStorageNode<PK,D>)master, (List<SortedMapStorageNode<PK,D>>)slaves);
			return new SortedMapStorageCallsiteAdapter<>(nodeParams, backingNode);
		}else if(StorageType.indexed == storageType){
			MasterSlaveIndexedSortedMapStorageNode<PK,D,F,IndexedSortedMapStorageNode<PK,D>> backingNode
					= new MasterSlaveIndexedSortedMapStorageNode<>(
					databeanClass, router,
					(IndexedSortedMapStorageNode<PK,D>)master, (List<IndexedSortedMapStorageNode<PK,D>>)slaves);
			return new IndexedSortedMapStorageCallsiteAdapter<>(nodeParams, backingNode);
		}else{
			throw new IllegalArgumentException("StorageType "+storageType+" not supported");
		}

	}


}
