package com.hotpads.handler.account;

import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.handler.user.DatarouterUserKey;

@Singleton
public class DatarouterUserAccountDao{

	@Inject
	private DatarouterAccountNodes datarouterAccountNodes;

	public Set<String> findAccountNamesForUser(DatarouterUserKey userKey){
		DatarouterUserAccountMapKey prefix = new DatarouterUserAccountMapKey(userKey.getId(), null);
		return datarouterAccountNodes.datarouterUserAccountMap().streamKeysWithPrefix(prefix, null)
				.map(DatarouterUserAccountMapKey::getDatarouterAccountKey)
				.map(DatarouterAccountKey::getAccountName)
				.collect(Collectors.toSet());

	}

}
