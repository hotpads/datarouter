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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.datarouter.exception.utils.nameparser.ExceptionSnapshot.ExceptionCauseSnapshot;
import io.datarouter.scanner.OptionalScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.tuple.Pair;

public abstract class ExceptionNameParserRegistry{

	private final Map<String, ExceptionNameParser> parserByTypeName = new HashMap<>();

	protected abstract void registerNameParsers();

	protected ExceptionNameParserRegistry(){
		registerNameParsers();
	}

	protected void register(ExceptionNameParser parser){
		String exceptionTypeName = parser.getTypeClassName();
		if(parserByTypeName.containsKey(exceptionTypeName)){
			throw new RuntimeException("type: " + exceptionTypeName + " was already registered");
		}
		parserByTypeName.put(exceptionTypeName, parser);
	}

	public Optional<Pair<ExceptionNameParser,ExceptionCauseSnapshot>> getNameParserAndCause(
			ExceptionSnapshot exception){
		return Scanner.of(parserByTypeName.values())
				.map(parser -> parser.getCauseFromType(exception))
				.concat(OptionalScanner::of)
				.findFirst()
				.map(cause -> new Pair<>(parserByTypeName.get(cause.typeName), cause));
	}

	public static class NoOpExceptionNameParserRegistry extends ExceptionNameParserRegistry{

		@Override
		protected void registerNameParsers(){
		}

	}

}
