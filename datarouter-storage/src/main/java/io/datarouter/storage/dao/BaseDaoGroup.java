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
import java.util.List;

import io.datarouter.util.Require;
import jakarta.inject.Singleton;

/**
 * A DaoGroup is a collection of related DAOs. It is easy to trace all uses of each DAO through your code using tools
 * like Eclipse's ctrl-shift-G shortcut.
 *
 * While a small application could have only one DaoGroup, a large application could group related DAOs into separate
 * DaoGroups. A good rule of thumb is to have one DaoGroup per database or per each major function of the application.
 *
 * A DAO can easily be moved between different DaoGroups. DaoGroups ensure that DAOs are only registered once. They also
 * ensure that all DAOs can be eagerly found so that schema updates can be performed before the application starts.
 */
@Singleton
public abstract class BaseDaoGroup{

	private final List<Class<? extends BaseDao>> daoClasses;

	public BaseDaoGroup(){
		this.daoClasses = new ArrayList<>();
	}

	public BaseDaoGroup add(Class<? extends BaseDao> daoClass){
		Require.notContains(daoClasses, daoClass, daoClass.getCanonicalName() + " is already registered");
		daoClasses.add(daoClass);
		return this;
	}

	public List<Class<? extends BaseDao>> getDaoClasses(){
		return daoClasses;
	}

}
