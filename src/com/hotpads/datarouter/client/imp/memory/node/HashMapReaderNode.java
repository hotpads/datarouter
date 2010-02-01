<<<<<<< HEAD:src/com/hotpads/datarouter/client/imp/hashmap/node/HashMapReaderNode.java
package com.hotpads.datarouter.client.imp.hashmap.node;

import java.util.Collection;
=======
package com.hotpads.datarouter.client.imp.memory.node;

import java.util.Collection;
import java.util.HashMap;
>>>>>>> origin/master:src/com/hotpads/datarouter/client/imp/memory/node/HashMapReaderNode.java
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
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
<<<<<<< HEAD:src/com/hotpads/datarouter/client/imp/hashmap/node/HashMapReaderNode.java
import com.hotpads.util.core.MapTool;
=======
>>>>>>> origin/master:src/com/hotpads/datarouter/client/imp/memory/node/HashMapReaderNode.java

public class HashMapReaderNode<D extends Databean> 
extends BasePhysicalNode<D>
implements PhysicalMapStorageReaderNode<D>
{
	
<<<<<<< HEAD:src/com/hotpads/datarouter/client/imp/hashmap/node/HashMapReaderNode.java
	protected Map<Key<D>,D> backingMap = MapTool.createHashMap();
=======
	protected Map<Key<D>,D> backingMap = new HashMap<Key<D>,D>();
>>>>>>> origin/master:src/com/hotpads/datarouter/client/imp/memory/node/HashMapReaderNode.java

	public HashMapReaderNode(Class<D> databeanClass, DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HashMapReaderNode(Class<D> databeanClass, DataRouter router, String clientName) {
		super(databeanClass, router, clientName);
	}

	@Override
	public HibernateClientImp getClient(){
		return (HibernateClientImp)this.router.getClient(getClientName());
	}
	
	@Override
	public Node<D> getMaster() {
		return null;
	}
	
	@Override
	public void clearThreadSpecificState(){
		//do nothing, i think
	}

	
	/************************************ MapStorageReader methods ****************************/
	
	@Override
	public boolean exists(Key<D> key, Config config) {
		return this.backingMap.containsKey(key);
	}

	
	@Override
	public D get(final Key<D> key, Config config) {
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
	public List<D> getMulti(final Collection<? extends Key<D>> keys, Config config) {		
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
