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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.datarouter.exception.utils.nameparser.ExceptionSnapshot.ExceptionCauseSnapshot;
import io.datarouter.scanner.Scanner;

public abstract class ExceptionNameParser{

	protected abstract Optional<String> parse(ExceptionCauseSnapshot exception);

	/**
	 * @return a list of throwables that should exist in the nested exceptions in order to perform name parsing
	 */
	protected abstract List<Class<? extends Throwable>> matchingTypes();

	/**
	 *
	 * @return an optional map that has key as the throwable existed in the nested exceptions and value representing one
	 *         of the stacktrace's declaringClasss and method names
	 */
	protected abstract Optional<Map<Class<? extends Throwable>, String>> stackTraceByType();

	public Set<String> getMatchingTypeClassNames(){
		return Scanner.of(matchingTypes())
				.map(Class::getName)
				.collect(Collectors.toSet());
	}

	public Optional<String> parseExceptionName(List<ExceptionCauseSnapshot> causes){
		return Scanner.of(causes)
				.concatOpt(this::parse)
				.findFirst();
	}

	public Optional<List<ExceptionCauseSnapshot>> getCausesFromType(ExceptionSnapshot exception){
		if(exception == null || !isCauseFromTypes(exception)){
			return Optional.empty();
		}
		Set<String> mathcingClasses = getMatchingTypeClassNames();
		return Optional.of(Scanner.of(exception.causes)
				.include(cause -> mathcingClasses.contains(cause.typeName))
				.list());
	}

	private boolean isCauseFromTypes(ExceptionSnapshot exception){
		List<String> allTypeNames = Scanner.of(exception.causes)
				.map(cause -> cause.typeName)
				.list();
		boolean hasAllMatchingTypes = allTypeNames.containsAll(getMatchingTypeClassNames());
		if(stackTraceByType().isEmpty() || !hasAllMatchingTypes){
			return hasAllMatchingTypes;
		}
		Map<String,String> stackTraceByTypeName = Scanner.of(stackTraceByType().get().entrySet())
				.toMap(entry -> entry.getKey().getName(), Entry::getValue);
		Optional<ExceptionCauseSnapshot> matchedCause = Scanner.of(exception.causes)
				.include(cause -> stackTraceByTypeName.containsKey(cause.typeName))
				.findFirst();
		if(matchedCause.isEmpty()){
			return false;
		}
		String matchedClassAndMethodInStackTrace = stackTraceByTypeName.get(matchedCause.get().typeName);
		return Scanner.of(matchedCause.get().stackTraces)
				.map(stackTrace -> stackTrace.declaringClass + "." + stackTrace.methodName)
				.include(matchedClassAndMethodInStackTrace::equals)
				.hasAny();
	}

}
