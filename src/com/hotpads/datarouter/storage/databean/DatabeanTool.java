package com.hotpads.datarouter.storage.databean;

import java.util.Collection;
import java.util.SortedMap;

import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;


public class DatabeanTool {

	public static <D extends Databean> SortedMap<PrimaryKey<D>,D> getByKeySorted(Collection<D> databeans){
		return KeyTool.getByKeySorted(databeans);
	}
	
}
