package com.hotpads.util.http.security;

import java.util.Collection;
import java.util.HashSet;

public class MultipleApiKeyPredicate implements ApiKeyPredicate{

	private String hotpadsApiKey;
	private final HashSet<String> keys;

	public MultipleApiKeyPredicate(String hotpadsApiKey, Collection<String> otherApiKeys){
		this.hotpadsApiKey = hotpadsApiKey;
		this.keys = new HashSet<>(otherApiKeys.size() + 1);
		this.keys.add(hotpadsApiKey);
		this.keys.addAll(otherApiKeys);
	}

	@Override
	public boolean check(String parameter){
		return keys.contains(parameter);
	}

	@Override
	public String getApiKey(){
		return hotpadsApiKey;
	}

}
