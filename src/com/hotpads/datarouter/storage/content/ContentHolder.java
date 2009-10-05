package com.hotpads.datarouter.storage.content;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;

public interface ContentHolder<D extends Databean> {

	boolean equalsContent(ContentHolder<D> other);
	
	Key<D> getKey();
}
