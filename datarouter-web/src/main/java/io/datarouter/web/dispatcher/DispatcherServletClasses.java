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
package io.datarouter.web.dispatcher;

import java.util.List;
import java.util.function.Supplier;

import javax.inject.Singleton;

@Singleton
public class DispatcherServletClasses implements Supplier<List<Class<? extends DispatcherServlet>>>{

	private final List<Class<? extends DispatcherServlet>> classes;

	@SafeVarargs
	public DispatcherServletClasses(Class<? extends DispatcherServlet>... classes){
		this(List.of(classes));
	}

	public DispatcherServletClasses(List<Class<? extends DispatcherServlet>> classes){
		this.classes = classes;
	}

	@Override
	public List<Class<? extends DispatcherServlet>> get(){
		return classes;
	}

}
