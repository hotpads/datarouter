package com.hotpads.joblet.handler;

import javax.inject.Inject;

import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.joblet.execute.JobletScaler;

/*
 * note: this is a standalone controller since it requires public access
 */
public class JobletScalingHandler extends BaseHandler {

	public static final String JSP = "/jsp/joblet/jobletScaling.jsp";

	@Inject
	private JobletScaler jobletScaler;
	@Inject
	private Datarouter datarouter;

	@Override
	@Handler
	protected Mav handleDefault(){
		return getRequiredJobletServers();
	}

	@Handler
	private Mav getRequiredJobletServers() {
		final Mav mav = new Mav(JSP);
		int numServers = jobletScaler.getNumJobletServers();
		mav.put("serverCount", numServers);
		mav.put("serverName", datarouter.getServerName());
		return mav;
	}
}
