package com.hotpads.webappinstance;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.util.core.cache.Cached;
import com.hotpads.webappinstance.databean.WebAppInstance;

@Singleton
public class CachedWebAppInstancesOfThisType extends Cached<List<WebAppInstance>>{

	private final DatarouterProperties datarouterProperties;
	private final WebAppInstanceDao webAppInstanceDao;


	@Inject
	public CachedWebAppInstancesOfThisType(DatarouterProperties datarouterProperties,
			WebAppInstanceDao webAppInstanceDao){
		super(20, TimeUnit.SECONDS);
		this.datarouterProperties = datarouterProperties;
		this.webAppInstanceDao = webAppInstanceDao;
	}


	@Override
	protected List<WebAppInstance> reload(){
		return webAppInstanceDao.getWebAppInstancesOfType(datarouterProperties.getServerType(), Duration.ofDays(1));
	}

}
