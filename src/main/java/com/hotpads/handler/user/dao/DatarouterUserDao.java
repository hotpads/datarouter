package com.hotpads.handler.user.dao;

import javax.inject.Singleton;

import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByApiKeyLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.exception.BadApiCallException;

@Singleton
public class DatarouterUserDao {
	
	private DatarouterUserNodes userNodes;
	
	public DatarouterUserDao(DatarouterUserNodes userNodes) {
		this.userNodes = userNodes;
	}
	
	public DatarouterUser lookupUserByApiKey(String apiKey) {
		if (StringTool.isNullOrEmpty(apiKey)) {
			throw new BadApiCallException("no api key specified");
		}

		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByApiKeyLookup(apiKey), null);

		if (user == null) {
			throw new BadApiCallException("no user found with provided api key");
		}
		if (BooleanTool.isFalseOrNull(user.isEnabled())) {
			throw new BadApiCallException("user is not enabled");
		}
		if (BooleanTool.isFalseOrNull(user.isApiEnabled())) {
			throw new BadApiCallException("user does not have api authorization");
		}

		return user;
	}
}
