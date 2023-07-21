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
package io.datarouter.job.detached;

import java.util.function.Supplier;

import io.datarouter.job.scheduler.JobWrapper;
import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginConfigValue;
import io.datarouter.plugin.PluginInjector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

public interface DetachedJobExecutor extends PluginConfigValue<DetachedJobExecutor>{

	PluginConfigKey<DetachedJobExecutor> KEY = new PluginConfigKey<>(
			"detachedJobExecutor",
			PluginConfigType.CLASS_SINGLE);

	void submit(JobWrapper jobWrapper);

	@Override
	default PluginConfigKey<DetachedJobExecutor> getKey(){
		return KEY;
	}

	@Singleton
	class DetachedJobExecutorSupplier implements Supplier<DetachedJobExecutor>{

		@Inject
		private PluginInjector injector;

		@Override
		public DetachedJobExecutor get(){
			return injector.getInstance(KEY);
		}

	}
}
