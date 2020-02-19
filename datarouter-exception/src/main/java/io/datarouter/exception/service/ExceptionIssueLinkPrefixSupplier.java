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

import java.util.function.Supplier;

import javax.inject.Singleton;

/**
 * Implementations of this class can be used for external services that track projects like JIRA
 */
public interface ExceptionIssueLinkPrefixSupplier extends Supplier<String>{

	@Singleton
	static class NoOpExceptionIssueLinkPrefixSupplier implements ExceptionIssueLinkPrefixSupplier{

		@Override
		public String get(){
			return "";
		}

	}

	@Singleton
	static class ExceptionIssueLinkPrefix implements ExceptionIssueLinkPrefixSupplier{

		private final String exceptionIssueLinkPrefix;

		public ExceptionIssueLinkPrefix(String exceptionIssueLinkPrefix){
			this.exceptionIssueLinkPrefix = exceptionIssueLinkPrefix;
		}

		@Override
		public String get(){
			return exceptionIssueLinkPrefix;
		}

	}

}
