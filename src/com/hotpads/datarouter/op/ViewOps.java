package com.hotpads.datarouter.op;

import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.View;

public interface ViewOps<PK extends PrimaryKey<PK>,UK extends UniqueKey<PK>> {

	Boolean isLatest(Key<PK> key);
	View<PK,UK> render(Key<PK> key);
	
}
