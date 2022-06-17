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
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.ReflectionTool;

public abstract class ExceptionNameParserRegistry{

	private final Map<Set<String>,Class<? extends ExceptionNameParser>> parserClassByTypeName = new HashMap<>();

	protected abstract void registerNameParsers();

	protected ExceptionNameParserRegistry(){
		registerNameParsers();
	}

	protected void register(Class<? extends ExceptionNameParser> parserClass){
		Set<String> parserTypeClassName = ReflectionTool.create(parserClass).getMatchingTypeClassNames();
		if(parserClassByTypeName.containsKey(parserTypeClassName)){
			throw new RuntimeException("type: " + parserTypeClassName + " was already registered");
		}
		parserClassByTypeName.put(parserTypeClassName, parserClass);
	}

	public List<Class<? extends ExceptionNameParser>> getNameParserClasses(){
		return Scanner.of(parserClassByTypeName.values())
				.list();
	}

	public static class NoOpExceptionNameParserRegistry extends ExceptionNameParserRegistry{

		@Override
		protected void registerNameParsers(){
		}

	}

}
