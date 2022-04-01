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
package io.datarouter.ratelimiter;

import java.time.Instant;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.util.DateTool;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.util.http.RequestTool;

public abstract class BaseRateLimiter{
	private static final Logger logger = LoggerFactory.getLogger(BaseRateLimiter.class);

	private static final String COUNTER_PREFIX = "RateLimiter ";

	private final String name;

	public BaseRateLimiter(String name){
		this.name = name;
	}

	public final boolean peek(String key){
		return internalAllow(makeKey(key), false).getLeft();
	}

	public final boolean allowed(){
		return allowed("");
	}

	public final boolean allowed(String dynamicKey){
		Pair<Boolean,Instant> allowed = internalAllow(makeKey(dynamicKey), true);
		if(allowed.getLeft()){
			Counters.inc(COUNTER_PREFIX + name + " allowed");
		}else{
			Counters.inc(COUNTER_PREFIX + name + " limit reached");
		}
		return allowed.getLeft();
	}

	public final boolean allowedForIp(HttpServletRequest request){
		return allowedForIp("", request);
	}

	public final boolean allowedForIp(String dynamicKey, HttpServletRequest request){
		String ip = RequestTool.getIpAddress(request);
		Pair<Boolean,Instant> allowed = internalAllow(makeKey(dynamicKey,ip), true);
		if(allowed.getLeft()){
			Counters.inc(COUNTER_PREFIX + "ip " + name + " allowed");
		}else{
			logger.info("RateLimiter={} limit reached for ip={}, next allowed {}",
					name,
					RequestTool.getIpAddress(request),
					DateTool.getYyyyMmDdHhMmSsMmmWithPunctuationNoSpaces(allowed.getRight().toEpochMilli()));
			Counters.inc(COUNTER_PREFIX + "ip " + name + " limit reached");
		}
		return allowed.getLeft();
	}

	private String makeKey(String... keyFields){
		return String.join("_", keyFields);
	}

	public String getName(){
		return name;
	}

	protected abstract Pair<Boolean,Instant> internalAllow(String key, boolean increment);

}
