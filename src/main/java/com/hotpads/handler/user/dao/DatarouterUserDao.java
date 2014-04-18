package com.hotpads.handler.user.dao;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByApiKeyLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.exception.InvalidApiCallException;

@Singleton
public class DatarouterUserDao {
	
	@Inject private DatarouterUserNodes userNodes;
	
	public DatarouterUserDao() {
		
	}
	
	public DatarouterUser lookupUserByApiKey(String apiKey) {
		if (StringTool.isNullOrEmpty(apiKey)) {
			throw new InvalidApiCallException("no api key specified");
		}

		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByApiKeyLookup(apiKey), null);

		if (user == null) {
			throw new InvalidApiCallException("no user found with provided api key");
		}
		if (BooleanTool.isFalseOrNull(user.getEnabled())) {
			throw new InvalidApiCallException("user is not enabled");
		}
		if (BooleanTool.isFalseOrNull(user.getApiEnabled())) {
			throw new InvalidApiCallException("user does not have api authorization");
		}

		return user;
	}
}
