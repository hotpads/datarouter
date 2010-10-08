package com.hotpads.datarouter.storage.view.index.multi;

import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;

public interface MultiIndexEntry<IK extends PrimaryKey<IK>,LK extends Lookup<TK>,TK extends PrimaryKey<TK>>
extends IndexEntry<IK,TK>{

	LK getLookup();
	
}
