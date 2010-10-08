package com.hotpads.datarouter.storage.view.index.unique;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;

public interface UniqueIndexEntry<IK extends PrimaryKey<IK>,UK extends UniqueKey<TK>,TK extends PrimaryKey<TK>>
extends IndexEntry<IK,TK>{

	UK getUniqueKey();
	
}
