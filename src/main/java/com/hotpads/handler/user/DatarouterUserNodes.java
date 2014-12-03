package com.hotpads.handler.user;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.handler.user.authenticate.api.ApiRequest;
import com.hotpads.handler.user.authenticate.api.ApiRequestKey;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.handler.user.session.DatarouterSessionKey;

public interface DatarouterUserNodes{

	IndexedSortedMapStorageNode<DatarouterUserKey,DatarouterUser> getUserNode();
	MapStorageNode<DatarouterSessionKey,DatarouterSession> getSessionNode();
	IndexedSortedMapStorageNode<ApiRequestKey, ApiRequest> getApiRequestNode();
}
