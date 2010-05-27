package com.hotpads.datarouter.client.imp.memory.node;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.client.imp.hibernate.HibernateClientImp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.base.physical.BasePhysicalNode;
import com.hotpads.datarouter.node.type.physical.PhysicalMapStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class HashMapReaderNode<D extends Databean,PK extends PrimaryKey<D>> 
extends BasePhysicalNode<D,PK>
implements PhysicalMapStorageReaderNode<D,PK>
{
	
	protected Map<UniqueKey<D>,D> backingMap = new HashMap<UniqueKey<D>,D>();

	public HashMapReaderNode(Class<PK> primaryKeyClass, 
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(primaryKeyClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HashMapReaderNode(Class<PK> primaryKeyClass, 
			DataRouter router, String clientName) {
		super(primaryKeyClass, router, clientName);
	}

	@Override
	public HibernateClientImp getClient(){
		return (HibernateClientImp)this.router.getClient(getClientName());
	}
	
	@Override
	public Node<D,PK> getMaster() {
		return null;
	}
	
	@Override
	public void clearThreadSpecificState(){
		//do nothing, i think
	}

	
	/************************************ MapStorageReader methods ****************************/
	
	@Override
	public boolean exists(UniqueKey<D> key, Config config) {
		return this.backingMap.containsKey(key);
	}

	
	@Override
	public D get(final UniqueKey<D> key, Config config) {
		return this.backingMap.get(key);
	}
	
	
	@Override
	public List<D> getAll(final Config config) {		
		List<D> result = ListTool.createArrayList(this.backingMap.size());
		for(D d : this.backingMap.values()){
			result.add(d);
		}
		return result;
	}

	
	@Override
	public List<D> getMulti(final Collection<? extends UniqueKey<D>> keys, Config config) {		
		List<D> result = ListTool.createLinkedList();
		for(Key<D> key : CollectionTool.nullSafe(keys)){
			D value = this.backingMap.get(key);
			if(value != null){
				result.add(value);
			}
		}
		return result;
	}

	
}
