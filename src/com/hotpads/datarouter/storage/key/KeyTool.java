package com.hotpads.datarouter.storage.key;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public class KeyTool {
	
	public static <D extends Databean, PK extends PrimaryKey<D>> Class<D> getDatabeanClass(Class<PK> primaryKeyClass){
		try{
			//use getDeclaredConstructor to access non-public constructors
			Constructor<PK> constructor = primaryKeyClass.getDeclaredConstructor();
			constructor.setAccessible(true);
			PrimaryKey<D> primaryKeyInstance = constructor.newInstance();
			return primaryKeyInstance.getDatabeanClass();
		}catch(Exception e){
			throw new DataAccessException(e.getClass().getSimpleName()+" on "+primaryKeyClass.getSimpleName()
					+".  Is there a no-arg constructor?");
		}
	}

	@SuppressWarnings("unchecked")
	public static <D extends Databean> List<PrimaryKey<D>> getKeys(Collection<D> databeans){
		List<PrimaryKey<D>> keys = ListTool.createLinkedList();
		for(D databean : CollectionTool.nullSafe(databeans)){
			keys.add(databean.getKey());
		}
		return keys;
	}

	@SuppressWarnings("unchecked")
	public static <D extends Databean> SortedMap<PrimaryKey<D>,D> getByKeySorted(Collection<D> databeans){
		SortedMap<PrimaryKey<D>,D> map = MapTool.createTreeMap();
		for(D databean : CollectionTool.nullSafe(databeans)){
			map.put(databean.getKey(), databean);
		}
		return map;
	}
	
	public static String getWhereClauseDisjunction(Collection<? extends Key<? extends Databean>> keys){
		if(CollectionTool.isEmpty(keys)){ return null; }
		StringBuilder sb = new StringBuilder();
		int counter = 0;
		for(Key<? extends Databean> key : keys){
			if(counter > 0){
				sb.append(" or ");
			}
			sb.append("("+key.getSqlNameValuePairsEscapedConjunction()+")");
			++counter;
		}
		return sb.toString();
	}
	
}
