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
package io.datarouter.storage.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Singleton;

@Singleton
public class DaoClasses implements Supplier<List<Class<? extends Dao>>>{

	private final List<Class<? extends Dao>> classes;

	public DaoClasses(){
		this.classes = new ArrayList<>();
	}

	@SafeVarargs
	public DaoClasses(Class<? extends Dao>... daoClassVarArgs){
		this(List.of(daoClassVarArgs));
	}

	public DaoClasses(Collection<Class<? extends Dao>> daoClasses){
		this.classes = new ArrayList<>();
		daoClasses.forEach(this::add);
	}

	@Override
	public List<Class<? extends Dao>> get(){
		return classes;
	}

	public DaoClasses add(Class<? extends Dao> daoClass){
		requireUnique(daoClass);
		classes.add(daoClass);
		return this;
	}

	public DaoClasses add(List<Class<? extends Dao>> daoClasses){
		daoClasses.forEach(this::add);
		return this;
	}

	private void requireUnique(Class<? extends Dao> daoClass){
		if(classes.contains(daoClass)){
			throw new IllegalArgumentException(daoClass.getCanonicalName() + " has already been registered");
		}
	}

}
