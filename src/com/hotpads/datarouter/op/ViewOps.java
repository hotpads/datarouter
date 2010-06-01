package com.hotpads.datarouter.op;

import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.View;

public interface ViewOps<PK extends PrimaryKey<PK>,K extends Key<PK>> {

	Boolean isLatest(Key<PK> key);
	View render(Key<PK> key);
	
}
