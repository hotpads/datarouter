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
package io.datarouter.job;

import io.datarouter.instrumentation.task.TaskTracker;

public abstract class BaseJob{

	public abstract void run(TaskTracker tracker) throws Exception;

	public String getPersistentName(){
		return getClass().getSimpleName();
	}

	@Override
	public String toString(){
		return getPersistentName();
	}

	public static Class<? extends BaseJob> parseClass(String className){
		try{
			return Class.forName(className).asSubclass(BaseJob.class);
		}catch(ClassNotFoundException e){
			throw new IllegalArgumentException(e);
		}
	}

}
