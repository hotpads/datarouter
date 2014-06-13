package com.hotpads.datarouter.storage.entity;

import java.util.List;
import java.util.NavigableMap;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public class EntityTool{
	
	public static <EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>>
	NavigableMap<EK,List<PK>> getPrimaryKeysByEntityKey(Iterable<PK> pks){
		NavigableMap<EK,List<PK>> pksByEntityKey = MapTool.createTreeMap();
		for(PK pk : IterableTool.nullSafe(pks)){
			EK ek = pk.getEntityKey();
			List<PK> pksForEntity = pksByEntityKey.get(ek);
			if(pksForEntity==null){
				pksForEntity = ListTool.createArrayList();
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
		NavigableMap<EK,List<D>> databeansByEntityKey = MapTool.createTreeMap();
		for(D databean : IterableTool.nullSafe(databeans)){
			EK ek = databean.getKey().getEntityKey();
			List<D> databeansForEntity = databeansByEntityKey.get(ek);
			if(databeansForEntity==null){
				databeansForEntity = ListTool.createArrayList();
				databeansByEntityKey.put(ek, databeansForEntity);
			}
			databeansForEntity.add(databean);
		}
		return databeansByEntityKey;
	}

}
