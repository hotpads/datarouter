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
package io.datarouter.exception.utils;

import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.datarouter.scanner.Scanner;

public class ExceptionDetailsDetector{

	public static ExceptionRecorderDetails detect(Throwable wholeException, Set<String> highlights){
		Throwable rootCause = ExceptionUtils.getRootCause(wholeException);
		Throwable exception = Optional.ofNullable(rootCause).orElse(wholeException);
		StackTraceElement stackTraceElement = searchClassName(exception, highlights)
				.orElseGet(() -> {
					StackTraceElement[] stackTrace = exception.getStackTrace();
					// stackTrace is often null in case of OOM
					return stackTrace.length == 0 ? null : stackTrace[0];
				});
		return new ExceptionRecorderDetails(exception, stackTraceElement);
	}

	private static Optional<StackTraceElement> searchClassName(Throwable cause, Set<String> highlights){
		return Scanner.of(cause.getStackTrace())
			.include(element -> Scanner.of(highlights)
					.anyMatch(highlight -> element.getClassName().contains(highlight)))
			.findFirst();
	}

	public static class ExceptionRecorderDetails{

		public final String className;
		public final String methodName;
		public final String type;
		public final int lineNumber;
		public final String detailsMessage;

		private ExceptionRecorderDetails(Throwable rootCause, StackTraceElement element){
			this.className = element == null ? "noClass" : element.getClassName();
			this.methodName = element == null ? "noMethod" : element.getMethodName();
			this.type = rootCause.getClass().getName();
			this.lineNumber = element == null ? 0 : element.getLineNumber();
			this.detailsMessage = rootCause.getMessage();
		}

	}

}
