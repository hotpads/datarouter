package com.hotpads.handler.user.authenticate.api;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;

public interface ApiRequestNode{
	
	IndexedSortedMapStorageNode<ApiRequestKey, ApiRequest> getApiRequestNode();

}
