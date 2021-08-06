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
package io.datarouter.web.shutdown;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.util.http.RequestTool;

@Singleton
public class ShutdownService{
	private static final Logger logger = LoggerFactory.getLogger(ShutdownService.class);

	public static final int SHUTDOWN_STATUS_CODE = 555;

	private static final DatarouterDuration AGE_TO_LOG = new DatarouterDuration(10, TimeUnit.SECONDS);

	private volatile ShutdownState state;
	private volatile long shutdownStartTime = Long.MAX_VALUE;

	public ShutdownService(){
		this.state = ShutdownState.RUNNING;
	}

	public int advance(){
		state = state.getNextState();
		if(shutdownStartTime == Long.MAX_VALUE){
			shutdownStartTime = System.currentTimeMillis();
		}
		return state.keepAliveErrorCode;
	}

	public boolean isShutdownOngoing(){
		return state != ShutdownState.RUNNING;
	}

	public int getKeepAliveErrorCode(){
		return state.keepAliveErrorCode;
	}

	private enum ShutdownState{
		OFF(null, SHUTDOWN_STATUS_CODE),
		SHUTTING_DOWN(OFF, 404),
		RUNNING(SHUTTING_DOWN, 200);

		private final ShutdownState nextState;
		private final int keepAliveErrorCode;

		ShutdownState(ShutdownState nextState, Integer keepAliveErrorCode){
			this.nextState = nextState;
			this.keepAliveErrorCode = keepAliveErrorCode;
		}

		public ShutdownState getNextState(){
			if(nextState == null){
				return this;
			}
			return nextState;
		}

	}

	public void logIfLate(HttpServletRequest request){
		DatarouterDuration age = DatarouterDuration.ageMs(shutdownStartTime);
		if(age.isLongerThan(AGE_TO_LOG)){
			logger.warn("receiving path={} age={} ageMs={}", RequestTool.getPath(request), age, age.toMillis());
		}else{
			logger.debug("receiving path={}", RequestTool.getPath(request));
		}
	}

}
