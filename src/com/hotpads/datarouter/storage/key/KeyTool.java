package com.hotpads.datarouter.storage.key;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public class KeyTool {
	
//	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> 
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

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> 
	List<PK> getKeys(Iterable<D> databeans){
		List<PK> keys = ListTool.createLinkedList();
		for(D databean : IterableTool.nullSafe(databeans)){
			keys.add(databean.getKey());
		}
		return keys;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> 
	Map<PK,D> getByKey(Iterable<D> databeans){
		Map<PK,D> map = MapTool.createHashMap();
		for(D databean : IterableTool.nullSafe(databeans)){
			map.put(databean.getKey(), databean);
		}
		return map;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>> 
	SortedMap<PK,D> getByKeySorted(Iterable<D> databeans){
		SortedMap<PK,D> map = MapTool.createTreeMap();
		for(D databean : IterableTool.nullSafe(databeans)){
			map.put(databean.getKey(), databean);
		}
		return map;
	}
	
	public static String getWhereClauseDisjunction(
			Collection<? extends FieldSet> fieldSets){
		if(CollectionTool.isEmpty(fieldSets)){ return null; }
		StringBuilder sb = new StringBuilder();
		int counter = 0;
		for(FieldSet fieldSet : IterableTool.nullSafe(fieldSets)){
			if(counter > 0){
				sb.append(" or ");
			}
			sb.append("("+fieldSet.getSqlNameValuePairsEscapedConjunction()+")");
			++counter;
		}
		return sb.toString();
	}
	
}
