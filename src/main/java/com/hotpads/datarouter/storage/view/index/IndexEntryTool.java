package com.hotpads.datarouter.storage.view.index;

import java.util.ArrayList;
import java.util.List;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrIterableTool;

public class IndexEntryTool{

	public static <IK extends PrimaryKey<IK>,
					PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					IE extends IndexEntry<IK,IE,PK,D>> 
	List<PK> getPrimaryKeys(Iterable<IE> indexEntries){
		List<PK> keys = new ArrayList<>();
		for(IE indexEntry : DrIterableTool.nullSafe(indexEntries)){
			keys.add(indexEntry.getTargetKey());
		}
		return keys;
	}
}
