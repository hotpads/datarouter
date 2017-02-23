package com.hotpads.webappinstance;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.util.core.cache.Cached;

@Singleton
public class CachedNumServersOfType extends Cached<Integer>{

	private final DatarouterProperties datarouterProperties;
	private final WebAppInstanceDao webAppInstanceDao;


	@Inject
	public CachedNumServersOfType(DatarouterProperties datarouterProperties, WebAppInstanceDao webAppInstanceDao){
		super(20, TimeUnit.SECONDS);
		this.datarouterProperties = datarouterProperties;
		this.webAppInstanceDao = webAppInstanceDao;
	}


	@Override
	protected Integer reload() {
		return webAppInstanceDao.getWebAppInstancesWithType(datarouterProperties.getServerType()).size();
	}

}
