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
package io.datarouter.exception.service;

import java.util.List;

import io.datarouter.exception.storage.summary.ExceptionRecordSummary;

public interface ExemptDailyDigestExceptions{

	List<ExemptDailyDigestException> getExemptExceptionAndLocations();

	default boolean isExempt(ExceptionRecordSummary exception){
		return getExemptExceptionAndLocations().stream()
				.anyMatch(dto -> exception.getKey().getType().equals(dto.type.getCanonicalName())
							&& exception.getKey().getExceptionLocation().equals(dto.location.getCanonicalName()));
	}

	class NoOpExemptDailyDigestExceptions implements ExemptDailyDigestExceptions{

		@Override
		public List<ExemptDailyDigestException> getExemptExceptionAndLocations(){
			return List.of();
		}

	}

	public static class ExemptDailyDigestException{

		private final Class<?> location;
		private final Class<? extends Exception> type;

		public ExemptDailyDigestException(Class<?> location, Class<? extends Exception> exception){
			this.location = location;
			this.type = exception;
		}

	}

}
