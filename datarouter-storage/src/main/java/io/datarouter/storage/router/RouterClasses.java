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
package io.datarouter.storage.router;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Singleton;

@Singleton
public class RouterClasses implements Supplier<List<Class<? extends Router>>>{

	private final List<Class<? extends Router>> classes;

	public RouterClasses(){
		this.classes = new ArrayList<>();
	}

	@SafeVarargs
	public RouterClasses(Class<? extends Router>... routerClassVarArgs){
		this(Arrays.asList(routerClassVarArgs));
	}

	public RouterClasses(Collection<Class<? extends Router>> routerClasses){
		this.classes = new ArrayList<>();
		routerClasses.forEach(this::add);
	}

	@Override
	public List<Class<? extends Router>> get(){
		return classes;
	}

	public RouterClasses add(Class<? extends Router> routerClass){
		requireUnique(routerClass);
		classes.add(routerClass);
		return this;
	}

	private void requireUnique(Class<? extends Router> routerClass){
		if(classes.contains(routerClass)){
			throw new IllegalArgumentException(routerClass.getCanonicalName() + " has already been registered");
		}
	}

}
