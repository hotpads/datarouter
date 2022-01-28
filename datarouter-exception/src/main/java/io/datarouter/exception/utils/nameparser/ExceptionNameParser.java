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

import java.util.Optional;

import io.datarouter.exception.utils.nameparser.ExceptionSnapshot.ExceptionCauseSnapshot;

public interface ExceptionNameParser{

	Optional<String> parse(ExceptionCauseSnapshot exception);

	Class<? extends Throwable> typeClass();

	default String getTypeClassName(){
		return typeClass().getName();
	}

	default Optional<String> parseExceptionName(Optional<ExceptionCauseSnapshot> cause){
		return cause
				.filter(this::isCauseFromType)
				.map(this::parse)
				.orElse(Optional.empty());
	}

	default Optional<ExceptionCauseSnapshot> getCauseFromType(ExceptionSnapshot exception){
		return exception == null ? Optional.empty()
				: exception.causes.stream()
						.filter(this::isCauseFromType)
						.findFirst();
	}

	default boolean isCauseFromType(ExceptionCauseSnapshot cause){
		return getTypeClassName().equals(cause.typeName);
	}

}