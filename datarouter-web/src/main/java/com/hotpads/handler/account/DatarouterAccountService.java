package com.hotpads.handler.account;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.handler.user.DatarouterUserKey;

@Singleton
public class DatarouterAccountService{

	@Inject
	private DatarouterAccountNodes datarouterAccountNodes;

	public Optional<DatarouterAccount> findAccountForApiKey(String apiKey){
		return datarouterAccountNodes.datarouterAccount().stream(null, null)
				.filter(account -> account.getApiKey().equals(apiKey))
				.findAny();
	}

	public Set<String> findAccountNamesForUser(DatarouterUserKey userKey){
		DatarouterUserAccountMapKey prefix = new DatarouterUserAccountMapKey(userKey.getId(), null);
		return datarouterAccountNodes.datarouterUserAccountMap().streamKeysWithPrefix(prefix, null)
				.map(DatarouterUserAccountMapKey::getDatarouterAccountKey)
				.map(DatarouterAccountKey::getAccountName)
				.collect(Collectors.toSet());

	}

	public Map<DatarouterUserKey,Set<String>> findAccountNamesForUsers(List<DatarouterUserKey> userKeys){
		List<DatarouterUserAccountMapKey> prefixes = userKeys.stream()
				.map(DatarouterUserKey::getId)
				.map(userId -> new DatarouterUserAccountMapKey(userId, null))
				.collect(Collectors.toList());
		return datarouterAccountNodes.datarouterUserAccountMap().streamKeysWithPrefixes(prefixes, null)
				.collect(Collectors.groupingBy(DatarouterUserAccountMapKey::getDatarouterUserKey,
						Collectors.mapping(DatarouterUserAccountMapKey::getDatarouterAccountKey,
						Collectors.mapping(DatarouterAccountKey::getAccountName,
						Collectors.toSet()))));

	}

}
