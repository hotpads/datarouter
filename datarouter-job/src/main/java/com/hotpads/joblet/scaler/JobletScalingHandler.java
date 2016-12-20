package com.hotpads.joblet.scaler;

import javax.inject.Inject;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;

/*
 * note: this is a standalone controller since it requires public access
 */
public class JobletScalingHandler extends BaseHandler{

	public static final String JSP = "/jsp/joblet/jobletScaling.jsp";

	@Inject
	private JobletScaler jobletScaler;
	@Inject
	private DatarouterProperties datarouterProperties;

	@Override
	@Handler
	protected Mav handleDefault(){
		return getRequiredJobletServers();
	}

	@Handler
	private Mav getRequiredJobletServers(){
		final Mav mav = new Mav(JSP);
		int numServers = jobletScaler.getNumJobletServers();
		mav.put("serverCount", numServers);
		mav.put("serverName", datarouterProperties.getServerName());
		return mav;
	}
}
