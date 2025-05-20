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

import io.datarouter.instrumentation.metric.Metrics;
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
	private static final String PAYLOAD_SIZE_BYTES = "payloadSizeBytes";

	@Inject
	private UserAgentTypeConfig userAgentTypeConfig;

	public void incMethodInvocation(Class<?> handler, String methodName, String userAgent){
		count(CALL);
		count(CLASS, handler.getSimpleName());
		count(PACKAGED_CLASS, handler.getName());
		count(METHOD, handler.getSimpleName(), methodName);
		count(PACKAGED_METHOD, handler.getName(), methodName);
		String categorizedUserAgent = userAgentTypeConfig.categorizeUserAgent(userAgent);
		count(USER_AGENT, categorizedUserAgent);
		count(CLASS, handler.getSimpleName(), USER_AGENT, categorizedUserAgent);
		count(METHOD, handler.getSimpleName(), methodName, USER_AGENT, categorizedUserAgent);
	}

	public static void incMethodInvocationByApiKeyPredicateName(Class<?> handler, String methodName,
			String accountName){
		count(ACCOUNT, accountName, CALL);
		count(ACCOUNT, accountName, CLASS, handler.getSimpleName());
		count(ACCOUNT, accountName, METHOD, handler.getSimpleName(), methodName);
	}

	public static void saveMethodLatency(Class<? extends BaseHandler> handlerClass, Method method, long durationMs){
		measureWithPercentiles(durationMs, LATENCY_MS);
		measureWithPercentiles(durationMs, CLASS, LATENCY_MS, handlerClass.getSimpleName());
		measureWithPercentiles(durationMs, METHOD, LATENCY_MS, handlerClass.getSimpleName(), method.getName());
	}

	public static void savePayloadSize(Class<? extends BaseHandler> handlerClass, Method method, long payloadSize){
		measureWithPercentiles(payloadSize, METHOD, PAYLOAD_SIZE_BYTES, handlerClass.getSimpleName(), method.getName());
	}

	public static void incDuration(Class<? extends BaseHandler> handlerClass, Method method, long durationMs){
		count(durationMs, METHOD, CUMULATED_DURATION_MS, handlerClass.getSimpleName(), method.getName());
	}

	public static void incTotalCpuTime(Class<? extends BaseHandler> handlerClass, Method method, long totalCpuTimeMs){
		count(totalCpuTimeMs, CUMULATED_CPU_MS);
		count(totalCpuTimeMs, METHOD, CUMULATED_CPU_MS, handlerClass.getSimpleName(), method.getName());
	}

	public static void incRequestBodyCollectionSize(Class<? extends BaseHandler> handlerClass, Method method,
			int batchSize){
		count(batchSize, METHOD, BATCH, handlerClass.getSimpleName(), method.getName());
	}

	private static void count(String... parts){
		count(1, parts);
	}

	private static void count(long value, String... parts){
		Metrics.count(PREFIX + " " + HANDLER + " " + String.join(" ", parts), value);
	}

	private static void measureWithPercentiles(long value, String... parts){
		Metrics.measureWithPercentiles(PREFIX + " " + HANDLER + " " + String.join(" ", parts), value);
	}

}
