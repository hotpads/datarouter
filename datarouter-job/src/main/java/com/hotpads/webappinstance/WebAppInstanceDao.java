package com.hotpads.webappinstance;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.app.WebAppName;
import com.hotpads.datarouter.config.Configs;
import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.monitoring.GitProperties;
import com.hotpads.datarouter.setting.ServerType;
import com.hotpads.webappinstance.databean.WebAppInstance;
import com.hotpads.webappinstance.databean.WebAppInstanceKey;

@Singleton
public class WebAppInstanceDao{

	public static final int ACCEPTABLE_REFRESH_DELAY_MINUTES = 20;

	//injected
	private final WebAppInstanceNodes webAppInstanceNodes;
	private final WebAppName webAppName;
	private final GitProperties gitProperties;
	private final DatarouterProperties datarouterProperties;

	//not injected
	private final Date startTime;

	@Inject
	public WebAppInstanceDao(WebAppInstanceNodes webAppInstanceNodes, WebAppName webAppName, GitProperties
			gitProperties, DatarouterProperties datarouterProperties){
		this.webAppInstanceNodes = webAppInstanceNodes;
		this.webAppName = webAppName;
		this.gitProperties = gitProperties;
		this.datarouterProperties = datarouterProperties;

		this.startTime = new Date(ManagementFactory.getRuntimeMXBean().getStartTime());
	}


	/***************** methods *************************/

	public List<WebAppInstance> getAll(){
		return webAppInstanceNodes.getWebAppInstance().stream(null, null)
				.collect(Collectors.toList());
	}

	public void updateWebAppInstanceTable(){
		webAppInstanceNodes.getWebAppInstance().put(new WebAppInstance(webAppName.getName(), datarouterProperties,
				startTime, gitProperties), null);
	}

	public WebAppInstanceKey getWebAppInstanceKey(){
		return new WebAppInstanceKey(webAppName.getName(), datarouterProperties.getServerName());
	}

	public List<String> getWebAppInstanceServerNamesForWebApp(String webAppName){
	    return webAppInstanceNodes.getWebAppInstance().streamWithPrefix(new WebAppInstanceKey(webAppName, null), null)
	    		.map(webAppInstance -> webAppInstance.getKey().getServerName()).collect(Collectors.toList());
	}

	/**
	 * Callers should use {@link WebAppInstance#getUniqueServerNames} on result if only serverNames are desired
	 * (not each webApp on the server)
	 */
	public List<WebAppInstance> getWebAppInstancesOfType(ServerType type, Duration heartbeatWithin){
		String typeString = type.getPersistentString();
		return webAppInstanceNodes.getWebAppInstance().stream(null, null)
				.filter(webAppInstance -> typeString.equals(webAppInstance.getServerType()))
				.filter(webAppInstance -> webAppInstance.getDurationSinceLastUpdated().compareTo(heartbeatWithin) < 0)
				.collect(Collectors.toList());
	}

	/**
	 * Callers should use {@link WebAppInstance#getUniqueServerNames} on result if only serverNames are desired
	 * (not each webApp on the server)
	 */
	public List<WebAppInstance> findInactiveWebAppInstances(){
		Iterable<WebAppInstance> scanner = webAppInstanceNodes.getWebAppInstance().scan(null, Configs.slaveOk());
		List<WebAppInstance> webAppsInstances = new ArrayList<>();
		for(WebAppInstance webAppInstance : scanner){
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, -ACCEPTABLE_REFRESH_DELAY_MINUTES);
			if(webAppInstance.getRefreshedLast() == null || webAppInstance.getRefreshedLast().before(cal.getTime())){
				webAppsInstances.add(webAppInstance);
			}
		}
		return webAppsInstances;
	}
}
