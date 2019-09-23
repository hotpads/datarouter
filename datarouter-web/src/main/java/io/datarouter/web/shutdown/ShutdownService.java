/**
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

import javax.inject.Singleton;

@Singleton
public class ShutdownService{

	private volatile ShutdownState state;

	public ShutdownService(){
		this.state = ShutdownState.RUNNING;
	}

	public int advance(){
		state = state.getNextState();
		return state.keepAliveErrorCode;
	}

	public boolean isShutdownOngoing(){
		return state != ShutdownState.RUNNING;
	}

	public int getKeepAliveErrorCode(){
		return state.keepAliveErrorCode;
	}

	private static enum ShutdownState{
		OFF(null, 555),
		SHUTTING_DOWN(OFF, 404),
		RUNNING(SHUTTING_DOWN, 200);

		private final ShutdownState nextState;
		private final int keepAliveErrorCode;

		ShutdownState(ShutdownState nextState, Integer keepAliveErrorCode){
			this.nextState = nextState;
			this.keepAliveErrorCode = keepAliveErrorCode;
		}

		public ShutdownState getNextState(){
			if(this == OFF){
				return this;
			}
			return nextState;
		}

	}

}
