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
package io.datarouter.web.handler;

import java.lang.reflect.Method;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.storage.metric.Gauges;
import io.datarouter.storage.util.DatarouterCounters;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class HandlerMetrics{

	private static final String PREFIX = DatarouterCounters.PREFIX;
	private static final String HANDLER = "handler";
	private static final String CALL = "call";
	private static final String CLASS = "class";
	private static final String PACKAGED_CLASS = "packagedClass";
	private static final String METHOD = "method";
	private static final String PACKAGED_METHOD = "packagedMethod";
	private static final String ACCOUNT = "account";
	private static final String LATENCY_MS = "latencyMs";
	private static final String CUMULATED_DURATION_MS = "cumulatedDurationMs";
	private static final String CUMULATED_CPU_MS = "cumulatedCpuMs";
	private static final String BATCH = "batch";
	private static final String USER_AGENT = "userAgent";

	@Inject
	private Gauges gauges;
	@Inject
	private UserAgentTypeConfig userAgentTypeConfig;

	public void incMethodInvocation(Class<?> handler, String methodName, String userAgent){
		incInternal(CALL);
		incInternal(CLASS, handler.getSimpleName());
		incInternal(PACKAGED_CLASS, handler.getName());
		incInternal(METHOD, handler.getSimpleName() + " " + methodName);
		incInternal(PACKAGED_METHOD, handler.getName() + " " + methodName);
		String categorizedUserAgent = categorizeUserAgent(userAgent);
		incInternal(USER_AGENT, categorizedUserAgent);
		incInternal(CLASS, String.join(" ", handler.getSimpleName(), USER_AGENT, categorizedUserAgent));
		incInternal(METHOD, String.join(" ", handler.getSimpleName(), methodName, USER_AGENT, categorizedUserAgent));
	}

	public String categorizeUserAgent(String userAgent){
		if(userAgent == null){
			return "null";
		}
		if(userAgentTypeConfig.getMobileUserAgents().stream().anyMatch(userAgent::contains)){
			if(userAgentTypeConfig.getAndroidUserAgents().stream().anyMatch(userAgent::contains)){
				return UserAgentTypeConfig.ANDROID_USER_AGENT;
			}
			if(userAgentTypeConfig.getIosUserAgents().stream().anyMatch(userAgent::contains)){
				return UserAgentTypeConfig.IOS_USER_AGENT;
			}
		}
		if(userAgentTypeConfig.getJavaUserAgents().stream().anyMatch(userAgent::contains)){
			return UserAgentTypeConfig.JAVA_USER_AGENT;
		}
		if(userAgentTypeConfig.getBotUserAgents().stream().anyMatch(userAgent::contains)){
			return UserAgentTypeConfig.BOT_USER_AGENT;
		}
		return UserAgentTypeConfig.WEB_USER_AGENT;
	}

	public static void incMethodInvocationByApiKeyPredicateName(Class<?> handler, String methodName,
			String accountName){
		incInternal(ACCOUNT + " " + accountName + " " + CALL);
		incInternal(ACCOUNT + " " + accountName + " " + CLASS, handler.getSimpleName());
		incInternal(ACCOUNT + " " + accountName + " " + METHOD, handler.getSimpleName() + " " + methodName);
	}

	private static void incInternal(String format){
		Counters.inc(PREFIX + " " + HANDLER + " " + format);
	}

	private static void incInternal(String format, String suffix){
		Counters.inc(PREFIX + " " + HANDLER + " " + format + " " + suffix);
	}

	public void saveMethodLatency(Class<? extends BaseHandler> handlerClass, Method method, long durationMs){
		gauges.save(PREFIX + " " + HANDLER + " " + METHOD + " " + LATENCY_MS + " " + handlerClass.getSimpleName() + " "
				+ method.getName(), durationMs);
	}

	public static void incDuration(Class<? extends BaseHandler> handlerClass, Method method, long durationMs){
		Counters.inc(PREFIX + " " + HANDLER + " " + METHOD + " " + CUMULATED_DURATION_MS + " " + handlerClass
				.getSimpleName() + " " + method.getName(), durationMs);
	}

	public static void incTotalCpuTime(Class<? extends BaseHandler> handlerClass, Method method, long totalCpuTimeMs){
		Counters.inc(PREFIX + " " + HANDLER + " " + METHOD + " " + CUMULATED_CPU_MS + " " + handlerClass.getSimpleName()
				+ " " + method.getName(), totalCpuTimeMs);
		Counters.inc(PREFIX + " " + HANDLER + " " + CUMULATED_CPU_MS, totalCpuTimeMs);
	}

	public static void incRequestBodyCollectionSize(Class<? extends BaseHandler> handlerClass, Method method,
			int batchSize){
		Counters.inc(PREFIX + " " + HANDLER + " " + METHOD + " " + BATCH + " " + handlerClass.getSimpleName() + " "
				+ method.getName(), batchSize);
	}

}
