package com.hotpads.handler.account;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage;

public interface DatarouterAccountNodes{

	SortedMapStorage<DatarouterAccountKey,DatarouterAccount> datarouterAccount();
	SortedMapStorage<DatarouterUserAccountMapKey,DatarouterUserAccountMap> datarouterUserAccountMap();

}
