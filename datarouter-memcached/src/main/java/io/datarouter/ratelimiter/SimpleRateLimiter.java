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
package io.datarouter.ratelimiter;

import io.datarouter.util.concurrent.ThreadTool;

public class SimpleRateLimiter{

	protected Long nextAllowed;
	private long rateLimitMilliseconds;

	public SimpleRateLimiter(long rateLimitMilliseconds){
		this.rateLimitMilliseconds = rateLimitMilliseconds;
		nextAllowed = System.currentTimeMillis();
	}

	public long getRateLimitMilliseconds(){
		return rateLimitMilliseconds;
	}

	public synchronized void next(){
		nextAllowed = System.currentTimeMillis() + rateLimitMilliseconds;
	}

	public synchronized void customDelay(long rateLimit){
		nextAllowed = System.currentTimeMillis() + rateLimit;
	}

	public synchronized boolean isAllowed(boolean wait){
		if(nextAllowed <= System.currentTimeMillis()){
			return true;
		}
		if(wait){
			pause(nextAllowed - System.currentTimeMillis());
		}
		return nextAllowed <= System.currentTimeMillis();
	}

	public synchronized void waitForPermission(boolean evenIfExtended, boolean incrementNext){
		while(!isAllowed(true) && evenIfExtended){
			// no op
		}
		if(incrementNext){
			next();
		}
	}

	private static void pause(long milliseconds){
		if(milliseconds < 1){
			return;
		}
		ThreadTool.sleep(milliseconds);
	}

}
