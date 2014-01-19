package com.hotpads.handler.user.session;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.Config;
import com.hotpads.handler.user.DatarouterUserNodes;

@Singleton
public class DatarouterSessionDao{
	
	private static final Long DEFAULT_SESSION_TIMEOUT_MS = 3 * 60 * 60 * 1000l;
	private static final Integer DEFAULT_SESSION_NUM_ATTEMPTS = 2;
	
	private static final Config DEFAULT_SESSION_CONFIG = new Config()
			.setTimeoutMs(DEFAULT_SESSION_TIMEOUT_MS)
			.setNumAttempts(DEFAULT_SESSION_NUM_ATTEMPTS)
			.setCacheTimeoutMs(75l); // memcached get timeout
	
	@Inject
	private DatarouterUserNodes userNodes;
	
	public void putTargetUrl(String sessionToken, String targetUrl){
		if (sessionToken == null || targetUrl == null){ return; }
		AuthenticationTargetUrl targetUrlDatabean = new AuthenticationTargetUrl(sessionToken, targetUrl);		
		userNodes.getAuthenticationTargetUrlNode().put(targetUrlDatabean, DEFAULT_SESSION_CONFIG);			
		
	}
	
}
