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
package io.datarouter.web.handler;

import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.storage.metric.Gauges;
import io.datarouter.storage.util.DatarouterCounters;

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

	@Inject
	private Gauges gauges;

	public void incMethodInvocation(Class<?> handler, String methodName){
		incInternal(CALL);
		incInternal(CLASS, handler.getSimpleName());
		incInternal(PACKAGED_CLASS, handler.getName());
		incInternal(METHOD, handler.getSimpleName() + " " + methodName);
		incInternal(PACKAGED_METHOD, handler.getName() + " " + methodName);
	}

	public void incMethodInvocationByApiKeyPredicateName(Class<?> handler, String methodName, String accountName){
		incInternal(ACCOUNT + " " + accountName + " " + CALL);
		incInternal(ACCOUNT + " " + accountName + " " + CLASS, handler.getSimpleName());
		incInternal(ACCOUNT + " " + accountName + " " + METHOD, handler.getSimpleName() + " " + methodName);
	}

	private void incInternal(String format){
		Counters.inc(PREFIX + " " + HANDLER + " " + format);
	}

	private void incInternal(String format, String suffix){
		Counters.inc(PREFIX + " " + HANDLER + " " + format + " " + suffix);
	}

	public void saveMethodLatency(Class<? extends BaseHandler> handlerClass, Method method, long durationMs){
		gauges.save(PREFIX + " " + HANDLER + " " + METHOD + " " + LATENCY_MS + " " + handlerClass.getSimpleName() + " "
				+ method.getName(), durationMs);
	}

}
