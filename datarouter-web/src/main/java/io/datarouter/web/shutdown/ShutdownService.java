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

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.util.http.RequestTool;
import jakarta.inject.Singleton;

@Singleton
public class ShutdownService{
	private static final Logger logger = LoggerFactory.getLogger(ShutdownService.class);

	public static final int SHUTDOWN_STATUS_CODE = 555;

	private static final DatarouterDuration AGE_TO_LOG = new DatarouterDuration(10, TimeUnit.SECONDS);

	private volatile ShutdownState state;
	private volatile long shutdownStartTime = Long.MAX_VALUE;

	public ShutdownService(){
		this.state = ShutdownState.WARMING;
	}

	public int advance(){
		state = state.getNextState();
		if(shutdownStartTime == Long.MAX_VALUE && state == ShutdownState.OFF){
			shutdownStartTime = System.currentTimeMillis();
		}
		logger.warn("advanced to next shutdown stage newState={} newStatusCode={}", state, state.probeStatusCode);
		return state.probeStatusCode;
	}

	public boolean isShutdownOngoing(){
		return state == ShutdownState.OFF;
	}

	public boolean isRunning(){
		return state == ShutdownState.RUNNING;
	}

	public int getProbeStatusCode(){
		return state.probeStatusCode;
	}

	public int getLbStatusCode(){
		return state.lbStatusCode;
	}

	private enum ShutdownState{
		OFF(null, SHUTDOWN_STATUS_CODE, SHUTDOWN_STATUS_CODE),
		RUNNING(OFF, 200, 200),
		FIRST_TRAFFIC(RUNNING, SHUTDOWN_STATUS_CODE, 200),
		WARMING(FIRST_TRAFFIC, SHUTDOWN_STATUS_CODE, SHUTDOWN_STATUS_CODE),
		;

		private final ShutdownState nextState;
		private final int probeStatusCode;
		private final int lbStatusCode;

		ShutdownState(ShutdownState nextState, int probeStatusCode, int lbStatusCode){
			this.nextState = nextState;
			this.probeStatusCode = probeStatusCode;
			this.lbStatusCode = lbStatusCode;
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

	public ShutdownState getState(){
		return state;
	}

}
