package com.hotpads.handler.user;

import javax.inject.Inject;

import com.hotpads.handler.user.DatarouterUser.DatarouterUserByApiKeyLookup;

public class CurrentDatarouterUserPredicate{

	@Inject
	private DatarouterUserNodes userNodes;

	public DatarouterUser get(String apiKey){
		return userNodes.getUserNode().lookupUnique(new DatarouterUserByApiKeyLookup(apiKey), null);
	}
}
