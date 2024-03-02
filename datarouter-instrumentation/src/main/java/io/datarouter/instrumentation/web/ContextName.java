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
package io.datarouter.instrumentation.web;

import java.util.function.Supplier;

import jakarta.inject.Singleton;

/**
 * root path segment
 *
 * inject this class when unable to inject ServletContextSuppleir
 *
 */
//TODO: Move class somewhere out of datarouter-instrumentation
@Singleton
public class ContextName implements Supplier<String>{

	private final String contextName;

	public ContextName(String contextName){
		this.contextName = contextName;
	}

	@Override
	public String get(){
		return contextName;
	}

	public String getContextPath(){
		return contextName == null ? "" : "/" + contextName;
	}

}
