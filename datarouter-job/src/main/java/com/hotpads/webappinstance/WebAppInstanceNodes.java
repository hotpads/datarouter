package com.hotpads.webappinstance;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.webappinstance.databean.WebAppInstance;
import com.hotpads.webappinstance.databean.WebAppInstanceKey;

public interface WebAppInstanceNodes{
	SortedMapStorageNode<WebAppInstanceKey,WebAppInstance> getWebAppInstance();
}
