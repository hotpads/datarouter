package com.hotpads.datarouter.op;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.view.View;

public interface ViewOps {

	Boolean isLatest(Key<? extends Databean> key);
	View render(Key<? extends Databean> key);
	
}
