package com.hotpads.server;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.server.databean.WebAppInstance;
import com.hotpads.server.databean.WebAppInstanceKey;

public interface WebAppInstanceNodes{
	SortedMapStorageNode<WebAppInstanceKey,WebAppInstance> getWebAppInstances();
}
