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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import javax.inject.Singleton;

@Singleton
public class RouterClasses implements Supplier<Set<Class<? extends Router>>>{

	private final Set<Class<? extends Router>> routerClasses;

	public RouterClasses(){
		routerClasses = new HashSet<>();
	}

	@SafeVarargs
	public RouterClasses(Class<? extends Router>... routerClassVarArgs){
		this.routerClasses = new HashSet<>(Arrays.asList(routerClassVarArgs));
	}

	@SafeVarargs
	public RouterClasses(Set<Class<? extends Router>> routerList, Class<? extends Router>... routerClassVarArgs){
		this.routerClasses = new HashSet<>(routerList);
		this.routerClasses.addAll(Arrays.asList(routerClassVarArgs));
	}

	@Override
	public Set<Class<? extends Router>> get(){
		return routerClasses;
	}

}
