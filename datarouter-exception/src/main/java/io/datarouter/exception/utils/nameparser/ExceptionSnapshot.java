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
package io.datarouter.exception.utils.nameparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.datarouter.scanner.WarnOnModifyList;

public class ExceptionSnapshot{

	public final List<ExceptionCauseSnapshot> causes;

	public ExceptionSnapshot(Throwable exception){
		List<ExceptionCauseSnapshot> causes = new ArrayList<>();
		while(exception != null){
			causes.add(new ExceptionCauseSnapshot(exception));
			exception = exception.getCause();
		}
		this.causes = causes;
	}

	public ExceptionSnapshot(List<ExceptionCauseSnapshot> causes){
		this.causes = causes;
	}

	public Optional<ExceptionCauseSnapshot> getRootCause(){
		return causes.isEmpty() ? Optional.empty() : Optional.of(causes.get(causes.size() - 1));
	}

	public static class ExceptionCauseSnapshot{

		public final String typeName;
		public final String simpleTypeName;
		public final String detailMessage;
		public final List<StackTraceElementSnapshot> stackTraces;

		public ExceptionCauseSnapshot(String typeName, String simpleTypeName, String detailMessage,
				List<StackTraceElementSnapshot> stackTraces){
			this.typeName = typeName;
			this.simpleTypeName = simpleTypeName;
			this.detailMessage = detailMessage;
			this.stackTraces = stackTraces;
		}

		public ExceptionCauseSnapshot(Class<? extends Throwable> typeClass, String detailMessage,
				List<StackTraceElementSnapshot> stackTraces){
			this(typeClass.getName(), typeClass.getSimpleName(), detailMessage, stackTraces);
		}

		public ExceptionCauseSnapshot(Throwable exception){
			this(exception.getClass(),
				exception.getMessage(),
				Arrays.stream(exception.getStackTrace())
						.map(StackTraceElementSnapshot::new)
						.collect(WarnOnModifyList.deprecatedCollector()));
		}

	}


	public static class StackTraceElementSnapshot{

		public final String declaringClass;
		public final String methodName;
		public final int lineNumber;

		public StackTraceElementSnapshot(StackTraceElement element){
			this(element.getClassName(), element.getMethodName(), element.getLineNumber());
		}

		public StackTraceElementSnapshot(String declaringClass, String methodName, int lineNumber){
			this.declaringClass = declaringClass;
			this.methodName = methodName;
			this.lineNumber = lineNumber;
		}
	}

}