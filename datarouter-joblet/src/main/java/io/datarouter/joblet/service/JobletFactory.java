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
package io.datarouter.joblet.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.joblet.model.Joblet;
import io.datarouter.joblet.model.JobletPackage;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletTypeFactory;
import io.datarouter.util.mutable.MutableBoolean;

@Singleton
public class JobletFactory{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private JobletTypeFactory jobletTypeFactory;

	public <P> Joblet<?> createForPackage(JobletPackage jobletPackage, MutableBoolean shutdownRequested){
		@SuppressWarnings("unchecked")
		JobletType<P> jobletType = (JobletType<P>)jobletTypeFactory.fromJobletRequest(
				jobletPackage.getJobletRequest());
		P jobletParams = JobletPackage.unmarshallJobletData(jobletType, jobletPackage);
		return create(jobletType, jobletPackage.getJobletRequest(), jobletParams, shutdownRequested);
	}

	private <P> Joblet<P> create(JobletType<P> jobletType, JobletRequest jobletRequest, P jobletParams,
			MutableBoolean shutdownRequested){
		Joblet<P> joblet = injector.getInstance(jobletType.getAssociatedClass());
		joblet.setJobletRequest(jobletRequest);
		joblet.setShutdownRequested(shutdownRequested);
		joblet.setJobletParams(jobletParams);
		return joblet;
	}

}
