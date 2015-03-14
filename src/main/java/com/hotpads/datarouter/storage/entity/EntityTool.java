package com.hotpads.datarouter.storage.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.core.DrIterableTool;

public class EntityTool{
	
	public static <EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>>
	NavigableMap<EK,List<PK>> getPrimaryKeysByEntityKey(Iterable<PK> pks){
		NavigableMap<EK,List<PK>> pksByEntityKey = new TreeMap<>();
		for(PK pk : DrIterableTool.nullSafe(pks)){
			EK ek = pk.getEntityKey();
			List<PK> pksForEntity = pksByEntityKey.get(ek);
			if(pksForEntity==null){
				pksForEntity = new ArrayList<>();
				pksByEntityKey.put(ek, pksForEntity);
			}
			pksForEntity.add(pk);
		}
		return pksByEntityKey;
	}
	
	public static <EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>>
	NavigableMap<EK,List<D>> getDatabeansByEntityKey(Iterable<D> databeans){
		NavigableMap<EK,List<D>> databeansByEntityKey = new TreeMap<>();
		for(D databean : DrIterableTool.nullSafe(databeans)){
			if(databean==null){ continue; }//seem to be getting some null entries from TraceFlushController?
			PK pk = databean.getKey();//leave on individual line for NPE trace
			EK ek = pk.getEntityKey();
			List<D> databeansForEntity = databeansByEntityKey.get(ek);
			if(databeansForEntity==null){
				databeansForEntity = new ArrayList<>();
				databeansByEntityKey.put(ek, databeansForEntity);
			}
			databeansForEntity.add(databean);
		}
		return databeansByEntityKey;
	}

}
