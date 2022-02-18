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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.exception.utils.nameparser.ExceptionNameParser;
import io.datarouter.exception.utils.nameparser.ExceptionNameParserRegistry;
import io.datarouter.exception.utils.nameparser.ExceptionSnapshot;
import io.datarouter.exception.utils.nameparser.ExceptionSnapshot.ExceptionCauseSnapshot;
import io.datarouter.exception.utils.nameparser.ExceptionSnapshot.StackTraceElementSnapshot;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.OptionalScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.tuple.Pair;

@Singleton
public class ExceptionDetailsDetector{

	@Inject
	private ExceptionNameParserRegistry registry;
	@Inject
	private DatarouterInjector injector;

	public ExceptionRecorderDetails detect(Throwable wholeException, String callOrigin, Set<String> highlights){
		return detect(new ExceptionSnapshot(wholeException), callOrigin, highlights);
	}

	public ExceptionRecorderDetails detect(ExceptionSnapshot snapshot, String callOrigin, Set<String> highlights){
		Optional<ExceptionCauseSnapshot> rootCause = snapshot.getRootCause();
		Optional<String> parsedName = Optional.empty();
		Optional<Pair<ExceptionNameParser,ExceptionCauseSnapshot>> parserAndCause = Scanner.of(registry
				.getNameParserClasses())
				.map(injector::getInstance)
				.map(ExceptionNameParser.class::cast)
				.map(parser -> parser.getCauseFromType(snapshot)
						.map(cause -> new Pair<>(parser, cause)))
				.concat(OptionalScanner::of)
				.findFirst();
		if(parserAndCause.isPresent()){
			rootCause = Optional.of(parserAndCause.get().getRight());
			ExceptionNameParser exceptionWithParser = parserAndCause.get().getLeft();
			parsedName = exceptionWithParser.parseExceptionName(rootCause);
		}
		ExceptionCauseSnapshot exception = rootCause
				.orElse(new ExceptionCauseSnapshot(null, null, null, List.of()));
		Optional<StackTraceElementSnapshot> stackTraceElement = searchClassName(exception, highlights);
		if(stackTraceElement.isEmpty()){
			stackTraceElement = exception.stackTraces.stream()
					.findFirst();
		}
		return new ExceptionRecorderDetails(exception, stackTraceElement, parsedName, callOrigin);
	}

	private static Optional<StackTraceElementSnapshot> searchClassName(ExceptionCauseSnapshot cause,
			Set<String> highlights){
		return Scanner.of(cause.stackTraces)
			.include(element -> Scanner.of(highlights)
					.anyMatch(highlight -> element.declaringClass.contains(highlight)))
			.findFirst();
	}

	public static class ExceptionRecorderDetails{

		public final String className;
		public final String methodName;
		public final String type;
		public final int lineNumber;
		public final String detailsMessage;
		public final String parsedName;

		private ExceptionRecorderDetails(ExceptionCauseSnapshot rootCause, Optional<StackTraceElementSnapshot> element,
				Optional<String> parsedName, String callOrigin){
			this.className = element.map(e -> e.declaringClass).orElse("noClass");
			this.methodName = element.map(e -> e.methodName).orElse("noMethod");
			this.type = rootCause.typeName;
			this.lineNumber = element.map(e -> e.lineNumber).orElse(0);
			this.detailsMessage = rootCause.detailMessage;
			this.parsedName = parsedName.orElse(getDefaultName(type, className, callOrigin));
		}

		public static String getDefaultName(String type, String className, String callOrigin){
			callOrigin = Optional.ofNullable(callOrigin).orElse(className);
			return String.format("%s at %s in %s", getSimpleClassName(type), getSimpleClassName(className),
					getSimpleClassName(callOrigin));
		}

		private static String getSimpleClassName(String fullClassName){
			return Optional.ofNullable(fullClassName)
					.map(ExceptionRecorderDetails::parseName)
					.orElse("");
		}

		private static String parseName(String fullClassName){
			String[] tokens = fullClassName.split("\\.");
			if(tokens.length < 2){
				return "";
			}else if(Character.isUpperCase(tokens[tokens.length - 1].charAt(0))){
				return tokens[tokens.length - 1];
			}else{
				return tokens[tokens.length - 2] + "." + tokens[tokens.length - 1];
			}
		}
	}
}
