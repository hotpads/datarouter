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

import javax.inject.Singleton;

import io.datarouter.storage.counter.Counters;
import io.datarouter.storage.util.DatarouterCounters;

@Singleton
public class HandlerCounters{

	private static final String
			PREFIX = DatarouterCounters.PREFIX,
			HANDLER = "handler",
			CALL = "call",
			CLASS = "class",
			PACKAGED_CLASS = "packagedClass",
			METHOD = "method",
			PACKAGED_METHOD = "packagedMethod";


	public void incMethodInvocation(BaseHandler handler, Method method){
		incInternal(CALL);
		incInternal(CLASS, handler.getClass().getSimpleName());
		incInternal(PACKAGED_CLASS, handler.getClass().getName());
		incInternal(METHOD, handler.getClass().getSimpleName() + " " + method.getName());
		incInternal(PACKAGED_METHOD, handler.getClass().getName() + " " + method.getName());
	}

	private void incInternal(String format){
		Counters.inc(PREFIX + " " + HANDLER + " " + format);
	}

	private void incInternal(String format, String suffix){
		Counters.inc(PREFIX + " " + HANDLER + " " + format + " " + suffix);
	}

}
