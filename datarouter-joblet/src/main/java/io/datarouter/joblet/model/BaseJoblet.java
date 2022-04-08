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
package io.datarouter.joblet.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.util.concurrent.UncheckedInterruptedException;
import io.datarouter.util.mutable.MutableBoolean;

public abstract class BaseJoblet<T> implements Joblet<T>{
	private static final Logger logger = LoggerFactory.getLogger(BaseJoblet.class);

	protected T params;
	protected JobletRequest jobletRequest;
	protected MutableBoolean shutdownRequested;

	@Override
	public JobletRequest getJobletRequest(){
		return jobletRequest;
	}

	@Override
	public void setJobletRequest(JobletRequest jobletRequest){
		this.jobletRequest = jobletRequest;
	}

	@Override
	public void setJobletParams(T params){
		this.params = params;
	}

	@Override
	public void setShutdownRequested(MutableBoolean shutdownRequested){
		this.shutdownRequested = shutdownRequested;
	}

	protected void assertShutdownNotRequested(){
		if(shutdownRequested.isTrue()){
			throw new UncheckedInterruptedException();
		}
	}

	protected void checkInterrupt(){
		if(Thread.interrupted()){
			logger.warn("setting shutdownRequested=true because of Thread.interrupted()");
			shutdownRequested.set(true);
			throw new UncheckedInterruptedException("interrupted");
		}
		if(shutdownRequested.isTrue()){
			logger.warn("throwing UIE because shutdownRequested");
			throw new UncheckedInterruptedException("shutdownRequested");
		}
	}

}
