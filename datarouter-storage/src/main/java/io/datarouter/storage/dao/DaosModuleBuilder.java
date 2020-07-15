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
package io.datarouter.storage.dao;

import java.util.List;

import io.datarouter.inject.guice.BaseGuiceModule;

/**
 * Marker interface to find list of Dao classes used in each module
 */
public abstract class DaosModuleBuilder extends BaseGuiceModule{

	public abstract List<Class<? extends Dao>> getDaoClasses();

	public static class EmptyDaosModuleBuilder extends DaosModuleBuilder{

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of();
		}

	}

}
