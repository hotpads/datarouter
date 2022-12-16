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
package io.datarouter.web.exception;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.storage.exception.ExceptionCategory;
import io.datarouter.web.monitoring.exception.ExceptionAndHttpRequestDto;

public interface ExceptionRecorder{

	Optional<ExceptionRecordDto> tryRecordException(Throwable exception, String callOrigin);
	Optional<ExceptionRecordDto> tryRecordException(Throwable exception, String callOrigin, ExceptionCategory category);
	Optional<ExceptionRecordDto> tryRecordException(Throwable exception, String callOrigin, ExceptionCategory category,
			List<String> additionalEmailRecipients);

	ExceptionRecordDto recordException(Throwable exception, ExceptionCategory category, String location,
			String methodName, String name, String type, Integer lineNumber, String callOrigin);

	ExceptionRecordDto recordException(Throwable exception, ExceptionCategory category, String location,
			String methodName, String name, String type, Integer lineNumber, String callOrigin,
			List<String> additionalEmailRecipients);

	Optional<ExceptionRecordDto> tryRecordExceptionAndHttpRequest(Throwable exception, String callOrigin,
			HttpServletRequest request);

	ExceptionRecordDto recordExceptionAndHttpRequest(Throwable exception, String location, String methodName,
			String name, String type, Integer lineNumber, HttpServletRequest request, String callOrigin);

	ExceptionRecordDto recordExceptionAndHttpRequest(ExceptionAndHttpRequestDto exceptionDto,
			ExceptionCategory category);

	void recordHttpRequest(HttpServletRequest request);

	class NoOpExceptionRecorder implements ExceptionRecorder{

		@Override
		public Optional<ExceptionRecordDto> tryRecordException(Throwable exception, String callOrigin){
			return Optional.empty();
		}

		@Override
		public Optional<ExceptionRecordDto> tryRecordException(Throwable exception, String callOrigin,
				ExceptionCategory category){
			return Optional.empty();
		}

		@Override
		public Optional<ExceptionRecordDto> tryRecordException(Throwable exception, String callOrigin,
				ExceptionCategory category, List<String> additionalEmailRecipients){
			return Optional.empty();
		}

		@Override
		public ExceptionRecordDto recordException(Throwable exception, ExceptionCategory category, String location,
				String methodName, String name, String type, Integer lineNumber, String callOrigin){
			return null;
		}

		@Override
		public ExceptionRecordDto recordException(Throwable exception, ExceptionCategory category, String location,
				String methodName, String name, String type, Integer lineNumber, String callOrigin,
				List<String> additionalEmailRecipients){
			return null;
		}

		@Override
		public Optional<ExceptionRecordDto> tryRecordExceptionAndHttpRequest(Throwable exception, String callOrigin,
				HttpServletRequest request){
			return Optional.empty();
		}

		@Override
		public ExceptionRecordDto recordExceptionAndHttpRequest(Throwable exception, String location, String methodName,
				String name, String type, Integer lineNumber, HttpServletRequest request, String callOrigin){
			return null;
		}

		@Override
		public ExceptionRecordDto recordExceptionAndHttpRequest(ExceptionAndHttpRequestDto exceptionDto,
				ExceptionCategory category){
			return null;
		}

		@Override
		public void recordHttpRequest(HttpServletRequest request){
		}

	}

}
