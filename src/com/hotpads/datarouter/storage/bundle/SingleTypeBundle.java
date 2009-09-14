package com.hotpads.datarouter.storage.bundle;

import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.util.core.CollectionTool;

public class SingleTypeBundle<D extends Databean> {

	NavigableMap<Key<D>,D> databeanByKey = new TreeMap<Key<D>,D>();
	
	@SuppressWarnings("unchecked")
	protected void add(D databean){
		if(databean==null){ return; }
		this.databeanByKey.put(databean.getKey(), databean);
	}

	protected void add(Collection<D> databeans){
		for(D databean : CollectionTool.nullSafe(databeans)){
			this.add(databean);
		}
	}
	
	protected D getFirst(){
		return CollectionTool.getFirst(this.databeanByKey.values());
	}
	
}
