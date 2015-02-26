package com.hotpads.datarouter.storage.view.index;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;

public class IndexEntryTool{

	public static <IK extends PrimaryKey<IK>,
					PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					IE extends IndexEntry<IK,IE,PK,D>> 
	List<PK> getPrimaryKeys(Collection<IE> indexEntries){
		List<PK> keys = DrListTool.createArrayListWithSize(indexEntries);
		for(IE indexEntry : DrIterableTool.nullSafe(indexEntries)){
			keys.add(indexEntry.getTargetKey());
		}
		return keys;
	}
}
