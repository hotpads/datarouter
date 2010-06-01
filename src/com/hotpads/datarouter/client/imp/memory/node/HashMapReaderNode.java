package com.hotpads.datarouter.client.imp.memory.node;

import java.util.Collection;
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
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public class HashMapReaderNode<D extends Databean<PK>,PK extends PrimaryKey<PK>> 
extends BasePhysicalNode<D,PK>
implements PhysicalMapStorageReaderNode<D,PK>
{
	
	protected Map<UniqueKey<PK>,D> backingMap = MapTool.createHashMap();

	public HashMapReaderNode(Class<D> databeanClass, 
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HashMapReaderNode(Class<D> databeanClass, 
			DataRouter router, String clientName) {
		super(databeanClass, router, clientName);
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
	public boolean exists(UniqueKey<PK> key, Config config) {
		return this.backingMap.containsKey(key);
	}

	
	@Override
	public D get(final UniqueKey<PK> key, Config config) {
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
	public List<D> getMulti(final Collection<? extends UniqueKey<PK>> keys, Config config) {		
		List<D> result = ListTool.createLinkedList();
		for(Key<PK> key : CollectionTool.nullSafe(keys)){
			D value = this.backingMap.get(key);
			if(value != null){
				result.add(value);
			}
		}
		return result;
	}

	
}
