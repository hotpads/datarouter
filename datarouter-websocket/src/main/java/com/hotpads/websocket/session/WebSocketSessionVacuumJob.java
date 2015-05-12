package com.hotpads.websocket.session;

import com.hotpads.datarouter.util.VacuumJob;
import com.hotpads.job.trigger.JobEnvironment;

public abstract class WebSocketSessionVacuumJob extends VacuumJob<WebSocketSessionKey, WebSocketSession>{

	private final PushService pushService;

	public WebSocketSessionVacuumJob(JobEnvironment jobEnvironment, WebSocketSessionNodeProvider nodeProvider,
			PushService pushService){
		super(jobEnvironment, nodeProvider.get());
		this.pushService = pushService;
	}

	@Override
	protected boolean shouldDelete(WebSocketSession webSocketSession){
		return !pushService.isAlive(webSocketSession);
	}

}
