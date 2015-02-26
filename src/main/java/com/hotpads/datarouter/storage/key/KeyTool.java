package com.hotpads.datarouter.storage.key;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrMapTool;

public class KeyTool {

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
	List<PK> getKeys(Iterable<D> databeans){
		List<PK> keys = DrListTool.createLinkedList();
		for(D databean : DrIterableTool.nullSafe(databeans)){
			keys.add(databean.getKey());
		}
		return keys;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
	Map<PK,D> getByKey(Iterable<D> databeans){
		Map<PK,D> map = DrMapTool.createHashMap();
		for(D databean : DrIterableTool.nullSafe(databeans)){
			map.put(databean.getKey(), databean);
		}
		return map;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
	SortedMap<PK,D> getByKeySorted(Iterable<D> databeans){
		SortedMap<PK,D> map = DrMapTool.createTreeMap();
		for(D databean : DrIterableTool.nullSafe(databeans)){
			map.put(databean.getKey(), databean);
		}
		return map;
	}
	
}
