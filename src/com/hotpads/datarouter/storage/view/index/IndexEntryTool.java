package com.hotpads.datarouter.storage.view.index;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

public class IndexEntryTool{


	public static <IK extends PrimaryKey<IK>,
					PK extends PrimaryKey<PK>,
					D extends Databean<PK>,
					IE extends IndexEntry<IK,PK,D>> 
	List<PK> getPrimaryKeys(Collection<IE> indexEntries){
		List<PK> keys = ListTool.createArrayListWithSize(indexEntries);
		for(IE indexEntry : IterableTool.nullSafe(indexEntries)){
			keys.add(indexEntry.getTargetKey());
		}
		return keys;
	}
}
