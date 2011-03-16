package com.hotpads.datarouter.storage.key;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public class KeyTool {
	
//	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
//	Class<D> getDatabeanClass(Class<PK> primaryKeyClass){
//		try{
//			//use getDeclaredConstructor to access non-public constructors
//			Constructor<PK> constructor = primaryKeyClass.getDeclaredConstructor();
//			constructor.setAccessible(true);
//			PK primaryKeyInstance = constructor.newInstance();
//			return primaryKeyInstance.getDatabeanClass();
//		}catch(Exception e){
//			throw new DataAccessException(e.getClass().getSimpleName()+" on "+primaryKeyClass.getSimpleName()
//					+".  Is there a no-arg constructor?");
//		}
//	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
	List<PK> getKeys(Iterable<D> databeans){
		List<PK> keys = ListTool.createLinkedList();
		for(D databean : IterableTool.nullSafe(databeans)){
			keys.add(databean.getKey());
		}
		return keys;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
	Map<PK,D> getByKey(Iterable<D> databeans){
		Map<PK,D> map = MapTool.createHashMap();
		for(D databean : IterableTool.nullSafe(databeans)){
			map.put(databean.getKey(), databean);
		}
		return map;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
	SortedMap<PK,D> getByKeySorted(Iterable<D> databeans){
		SortedMap<PK,D> map = MapTool.createTreeMap();
		for(D databean : IterableTool.nullSafe(databeans)){
			map.put(databean.getKey(), databean);
		}
		return map;
	}
	
}
