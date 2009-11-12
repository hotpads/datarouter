package com.hotpads.datarouter.storage.databean;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.KeyTool;


public class DatabeanTool {

	public static <D extends Databean> List<Key<D>> getKeys(Collection<D> databeans){
		return KeyTool.getKeys(databeans);
	}

	public static <D extends Databean> SortedMap<Key<D>,D> getByKeySorted(Collection<D> databeans){
		return KeyTool.getByKeySorted(databeans);
	}
	
}
