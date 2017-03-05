package com.hotpads.webappinstance;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.util.core.cache.Cached;

@Singleton
public class CachedNumServersAliveOfThisType extends Cached<Integer>{

	private static final Duration HEARTBEAT_WITHIN = Duration.ofMinutes(3);

	private final DatarouterProperties datarouterProperties;
	private final WebAppInstanceDao webAppInstanceDao;


	@Inject
	public CachedNumServersAliveOfThisType(DatarouterProperties datarouterProperties,
			WebAppInstanceDao webAppInstanceDao){
		super(20, TimeUnit.SECONDS);
		this.datarouterProperties = datarouterProperties;
		this.webAppInstanceDao = webAppInstanceDao;
	}


	@Override
	protected Integer reload(){
		return webAppInstanceDao.getWebAppInstancesOfType(datarouterProperties.getServerType(), HEARTBEAT_WITHIN)
				.size();
	}

}
