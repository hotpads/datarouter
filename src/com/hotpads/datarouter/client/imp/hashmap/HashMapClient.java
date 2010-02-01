package com.hotpads.datarouter.client.imp.hashmap;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.util.core.CollectionTool;


public class HashMapClient implements Client{

	String name;
	public String getName(){
		return name;
	}
	
	private Map<Key<Databean>,Databean> map = new HashMap<Key<Databean>,Databean>();

	public <D extends Databean> void delete(Key<Databean> key, Config config) {
		this.map.remove(key);
		
	}

	public void deleteAll(Config config) {
		this.map.clear();
	}

	public <D extends Databean> void deleteMulti(Collection<Key<D>> keys, Config config) {
		for(Key<D> key : CollectionTool.nullSafe(keys)){
			this.map.remove(key);
		}
	}

	@SuppressWarnings("unchecked")
	public <D extends Databean> void put(D entity, Config config) {
		this.map.put(entity.getKey(), entity);
	}

	@SuppressWarnings("unchecked")
	public <D extends Databean> void putMulti(Collection<D> entities, Config config){
		for(Databean entity : CollectionTool.nullSafe(entities)){
			this.map.put(entity.getKey(), entity);
		}
	}

	public <D extends Databean> boolean exists(Key<D> key, Config config) throws HibernateException {
		return this.map.containsKey(key);
	}

	@SuppressWarnings("unchecked")
	public <D extends Databean> D get(Key<D> key, Config config) throws HibernateException {
		return (D)this.map.get(key);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		List<String> attributes = new LinkedList<String>();
		attributes.add("name:"+name);
		attributes.add("numEntities:"+this.map.size());
		sb.append("HashMap[");
		sb.append(CollectionTool.getCsvList(attributes));
		sb.append("]");
		return sb.toString();
	}
	
}
