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
package io.datarouter.inject.guice;

import java.util.List;

import io.datarouter.instrumentation.description.Describeable;
import io.datarouter.instrumentation.description.Description;

public abstract class BasePlugin extends BaseGuiceModule implements Describeable{

	/**
	 * The name is used to identify which plugins have already been added, and which can be overridden.
	 * Names have to be unique and can be easily changed.
	 *
	 * @return the name of the plugin
	 */
	public final String getName(){
		return getClass().getSimpleName();
	}

	@Override
	public Description describe(){
		return new Description(getName(), null, List.of());
	}

	/**
	 * This is experimental.
	 */
	public BaseGuiceModule getAsDefaultBinderModule(){
		return new BaseGuiceModule(){

			@Override
			protected void configure(){
			}

		};
	}

}
