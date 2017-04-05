package com.hotpads.joblet.scaler;

import java.time.Duration;
import java.util.List;

import javax.inject.Inject;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.webappinstance.WebAppInstanceDao;
import com.hotpads.webappinstance.databean.WebAppInstance;

/*
 * note: this is a standalone controller since it requires public access
 */
public class JobletScalingHandler extends BaseHandler{

	public static final String PATH = "/jobletScaling";
	public static final String JSP = "/jsp/joblet/jobletScaling.jsp";

	@Inject
	private WebAppInstanceDao webAppInstanceDao;
	@Inject
	private JobletScaler jobletScaler;
	@Inject
	private DatarouterProperties datarouterProperties;


	/**
	 * @param jobletServerType serverType of a jobletServer with per-instance and cluster thread limits configured
	 * @return the recommended number of joblet servers to run
	 */
	@Handler(defaultHandler = true)
	Mav getRecommendedJobletServerCount(String jobletServerType){
		final Mav mav = new Mav(JSP);
		int numServers = jobletScaler.getNumJobletServers(findJobletWebAppInstance(jobletServerType));
		mav.put("serverCount", numServers);
		mav.put("serverName", datarouterProperties.getServerName());
		return mav;
	}


	//optionally override this in a subclass handler
	protected WebAppInstance findJobletWebAppInstance(String serverTypeString){
		List<WebAppInstance> jobletInstances = webAppInstanceDao.getWebAppInstancesWithTypeString(serverTypeString,
				Duration.ofMinutes(2));
		return DrCollectionTool.getFirst(jobletInstances);
	}
}
