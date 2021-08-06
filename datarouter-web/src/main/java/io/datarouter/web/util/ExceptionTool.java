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
package io.datarouter.web.util;

import org.apache.logging.log4j.core.impl.ThrowableProxy;

public class ExceptionTool{

	/**
	 * This should NOT be used for logging. Instead of this use
	 * <br>
	 * <br>
	 * <code>logger.warn("CustomExceptionResolver caught an exception:", ex)</code>
	 * <br>
	 * <br>
	 * or at least(If you have nothing to say)
	 * <br>
	 * <br>
	 * <code>logger.warn("", ex);</code>
	 */
	public static String getStackTraceAsString(Throwable exception){
		ThrowableProxy proxy = new ThrowableProxy(exception);
		return proxy.getCauseStackTraceAsString("");
	}

	@SafeVarargs
	public static boolean isFromInstanceOf(Throwable exception, Class<? extends Exception>... classes){
		while(exception != null){
			for(Class<? extends Exception> clazz : classes){
				if(clazz.isAssignableFrom(exception.getClass())){
					return true;
				}
			}
			exception = exception.getCause();
		}
		return false;
	}

}
