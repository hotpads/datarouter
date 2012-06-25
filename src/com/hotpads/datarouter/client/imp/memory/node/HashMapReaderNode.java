package com.hotpads.datarouter.client.imp.memory.node;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hotpads.datarouter.client.imp.hibernate.HibernateClientImp;
import com.hotpads.datarouter.client.imp.memory.MemoryClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class HashMapReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
extends BasePhysicalNode<PK,D,F>
implements MapStorageReader<PK,D>{
	
	protected Map<UniqueKey<PK>,D> backingMap = new ConcurrentHashMap<UniqueKey<PK>,D>();
	
	
	public HashMapReaderNode(Class<D> databeanClass, Class<F> fielderClass, 
			DataRouter router, MemoryClient client) {
		super(databeanClass, fielderClass, router, client.getName());
		client.registerNode(this);
	}

	
	@Override
	public HibernateClientImp getClient(){
		return (HibernateClientImp)router.getClient(getClientName());
	}
	
	@Override
	public Node<PK,D> getMaster() {
		return null;
	}
	
	@Override
	public List<Node<PK,D>> getChildNodes(){
		return ListTool.create();
	}
	
	@Override
	public void clearThreadSpecificState(){
		//do nothing, i think
	}

	
	/************************************ MapStorageReader methods ****************************/
	
	@Override
	public boolean exists(PK key, Config config) {
		return backingMap.containsKey(key);
	}

	
	@Override
	public D get(final PK key, Config config) {
		return backingMap.get(key);
	}
	
	
	@Override
	public List<D> getAll(final Config config) {		
		List<D> result = ListTool.createArrayList(backingMap.size());
		for(D d : backingMap.values()){
			result.add(d);
		}
		return result;
	}

	
	@Override
	public List<D> getMulti(final Collection<PK> keys, Config config) {		
		List<D> result = ListTool.createLinkedList();
		for(Key<PK> key : CollectionTool.nullSafe(keys)){
			D value = backingMap.get(key);
			if(value != null){
				result.add(value);
			}
		}
		return result;
	}

	
	@Override
	public List<PK> getKeys(final Collection<PK> keys, Config config) {		
		List<PK> result = ListTool.createLinkedList();
		for(Key<PK> key : CollectionTool.nullSafe(keys)){
			D value = backingMap.get(key);
			if(value != null){
				result.add(value.getKey());
			}
		}
		return result;
	}

	
	/*********************** stats ********************************/
	
	public int getSize(){
		return backingMap.size();
	}
}
