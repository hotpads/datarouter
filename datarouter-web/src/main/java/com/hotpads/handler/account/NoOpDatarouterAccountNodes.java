package com.hotpads.handler.account;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage;

public class NoOpDatarouterAccountNodes implements DatarouterAccountNodes{

	@Override
	public SortedMapStorage<DatarouterAccountKey,DatarouterAccount> datarouterAccount(){
		return null;
	}

	@Override
	public SortedMapStorage<DatarouterUserAccountMapKey,DatarouterUserAccountMap> datarouterUserAccountMap(){
		return null;
	}

}
