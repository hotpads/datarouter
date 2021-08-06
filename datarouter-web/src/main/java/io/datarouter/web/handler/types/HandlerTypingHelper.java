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
package io.datarouter.web.handler.types;

import java.lang.reflect.Method;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.handler.BaseHandler.Handler;
import io.datarouter.web.handler.BaseHandler.NullHandlerDecoder;

@Singleton
public class HandlerTypingHelper{

	@Inject
	private DatarouterInjector injector;

	/**
	 * This methods goes through all methods who are named like methodName and tries to find the one that has the
	 * largest number of parameters. It generates the array of arguments at the same time.
	 */
	public Pair<Method,Object[]> findMethodByName(Collection<Method> possibleMethods,
			Class<? extends HandlerDecoder> handlerDecoderClass, HttpServletRequest request){
		Method method = null;
		Object[] args = new Object[]{};
		for(Method possibleMethod : possibleMethods){
			Class<? extends HandlerDecoder> decoderClass = handlerDecoderClass;
			Class<? extends HandlerDecoder> methodDecoder = possibleMethod.getAnnotation(Handler.class).decoder();
			if(!methodDecoder.equals(NullHandlerDecoder.class)){
				decoderClass = methodDecoder;
			}
			HandlerDecoder decoder = injector.getInstance(decoderClass);
			Object[] newArgs;
			String traceName = decoder.getClass().getSimpleName() + " decode";
			try(var $ = TracerTool.startSpan(traceName, TraceSpanGroupType.SERIALIZATION)){
				newArgs = decoder.decode(request, possibleMethod);
			}
			if(newArgs == null){
				continue;
			}
			if(args.length < newArgs.length || args.length == 0){
				args = newArgs;
				method = possibleMethod;
			}
		}
		return new Pair<>(method, args);
	}

}
