package com.hotpads.handler.user;

import com.hotpads.datarouter.node.op.combo.IndexedMapStorage.IndexedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.handler.user.session.AuthenticationTargetUrl;
import com.hotpads.handler.user.session.AuthenticationTargetUrlKey;

public interface DatarouterUserNodes{

	IndexedMapStorageNode<DatarouterUserKey,DatarouterUser> getUserNode();
	MapStorageNode<AuthenticationTargetUrlKey,AuthenticationTargetUrl> getAuthenticationTargetUrlNode();
}
