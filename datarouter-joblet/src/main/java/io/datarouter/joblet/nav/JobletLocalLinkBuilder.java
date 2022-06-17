/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.joblet.nav;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.joblet.config.DatarouterJobletPaths;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.handler.JobletExceptionHandler;
import io.datarouter.joblet.handler.JobletHandler;
import io.datarouter.joblet.handler.JobletQueuesHandler;
import io.datarouter.joblet.handler.JobletUpdateHandler;
import io.datarouter.joblet.handler.RunningJobletsHandler;
import io.datarouter.joblet.type.JobletType;

@Singleton
public class JobletLocalLinkBuilder{

	@Inject
	private DatarouterJobletPaths paths;

	public String exceptions(String contextPath, String jobletType){
		return new URIBuilder()
				.setPath(contextPath + paths.datarouter.joblets.exceptions.toSlashedString())
				.addParameter(JobletExceptionHandler.P_typeString, jobletType)
				.toString();
	}

	public String queues(String contextPath, String jobletType, int executionOrder, int numQueueIds){
		if(numQueueIds == 0){
			return null;
		}
		return new URIBuilder()
				.setPath(contextPath + paths.datarouter.joblets.queues.toSlashedString())
				.addParameter(JobletQueuesHandler.P_jobletType, jobletType)
				.addParameter(JobletQueuesHandler.P_executionOrder, executionOrder + "")
				.toString();
	}

	public String listWithStatus(String contextPath, JobletStatus status){
		return new URIBuilder()
				.setPath(contextPath + paths.datarouter.joblets.list.toSlashedString())
				.addParameter(JobletHandler.PARAM_whereStatus, status.persistentString)
				.toString();
	}

	public String requeue(String contextPath, JobletType<?> type){
		var uriBuilder = new URIBuilder()
				.setPath(contextPath + paths.datarouter.joblets.copyJobletRequestsToQueues.toSlashedString());
		if(type != null){
			uriBuilder.addParameter(JobletUpdateHandler.PARAM_jobletType, type.getPersistentString());
		}
		return uriBuilder.toString();
	}

	public String restart(String contextPath, JobletType<?> type, JobletStatus status){
		var uriBuilder = new URIBuilder()
				.setPath(contextPath + paths.datarouter.joblets.restart.toSlashedString());
		if(type != null){
			uriBuilder.addParameter(JobletUpdateHandler.PARAM_jobletType, type.getPersistentString());
		}
		return uriBuilder.addParameter(JobletUpdateHandler.PARAM_status, status.persistentString)
				.toString();
	}

	public String delete(String contextPath, String jobletType, int executionOrder, JobletStatus status){
		return new URIBuilder()
				.setPath(contextPath + paths.datarouter.joblets.deleteGroup.toSlashedString())
				.addParameter(JobletUpdateHandler.PARAM_jobletType, jobletType)
				.addParameter(JobletUpdateHandler.PARAM_executionOrder, executionOrder + "")
				.addParameter(JobletUpdateHandler.PARAM_status, status.persistentString)
				.toString();
	}

	public String kill(String contextPath, String id){
		return new URIBuilder()
				.setPath(contextPath + paths.datarouter.joblets.kill.toSlashedString())
				.addParameter(RunningJobletsHandler.P_threadId, id)
				.toString();
	}

}
