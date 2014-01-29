package com.hotpads.handler.user;

import com.hotpads.datarouter.node.op.combo.IndexedMapStorage.IndexedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.handler.user.session.DatarouterSessionKey;

public interface DatarouterUserNodes{

	IndexedMapStorageNode<DatarouterUserKey,DatarouterUser> getUserNode();
	MapStorageNode<DatarouterSessionKey,DatarouterSession> getSessionNode();
}
