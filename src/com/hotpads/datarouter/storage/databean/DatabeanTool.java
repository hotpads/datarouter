package com.hotpads.datarouter.storage.databean;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;


public class DatabeanTool {

	@SuppressWarnings("unchecked")
	public static <D extends Databean> List<Key<D>> getKeys(Collection<D> databeans){
		List<Key<D>> keys = ListTool.createLinkedList();
		for(D databean : CollectionTool.nullSafe(databeans)){
			keys.add(databean.getKey());
		}
		return keys;
	}

	@SuppressWarnings("unchecked")
	public static <D extends Databean> SortedMap<Key<D>,D> getByKeySorted(Collection<D> databeans){
		SortedMap<Key<D>,D> map = MapTool.createTreeMap();
		for(D databean : CollectionTool.nullSafe(databeans)){
			map.put(databean.getKey(), databean);
		}
		return map;
	}
	
}
