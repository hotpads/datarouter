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

import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.core.impl.ThrowableProxy;

import io.datarouter.util.concurrent.UncheckedInterruptedException;
import io.datarouter.util.string.StringTool;

public class ExceptionTool{

	private static final int MAX_STACK_TRACE_LINE_LENGTH = 2000;

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
		return proxy.getCauseStackTraceAsString("").lines()
				.map(line -> StringTool.trimToSizeAndLog(line, MAX_STACK_TRACE_LINE_LENGTH, "[trimmed]", "stacktrace",
						exception.getClass().getSimpleName()))
				.collect(Collectors.joining("\n"));
	}

	@SafeVarargs
	public static boolean isFromInstanceOf(Throwable exception, Class<? extends Exception>... classes){
		return getFirstMatchingExceptionIfApplicable(exception, classes).isPresent();
	}

	@SafeVarargs
	public static Optional<Throwable> getFirstMatchingExceptionIfApplicable(Throwable exception,
			Class<? extends Exception>... classes){
		while(exception != null){
			for(Class<? extends Exception> clazz : classes){
				if(clazz.isAssignableFrom(exception.getClass())){
					return Optional.of(exception);
				}
			}
			exception = exception.getCause();
		}
		return Optional.empty();
	}

	public static boolean isInterrupted(Throwable exception){
		// http client timeouts are SocketTimeoutException
		// there are not interruption in the sense of a program shutdown or similar
		if(isFromInstanceOf(exception, SocketTimeoutException.class)){
			return false;
		}
		return isFromInstanceOf(exception, InterruptedException.class, UncheckedInterruptedException.class,
				InterruptedIOException.class);
	}

}
