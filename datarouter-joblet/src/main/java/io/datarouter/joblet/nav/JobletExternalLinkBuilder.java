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
package io.datarouter.joblet.nav;

import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginConfigValue;
import io.datarouter.plugin.PluginInjector;

public interface JobletExternalLinkBuilder extends PluginConfigValue<JobletExternalLinkBuilder>{

	PluginConfigKey<JobletExternalLinkBuilder> KEY = new PluginConfigKey<>(
			"jobletExternalLinkBuilder",
			PluginConfigType.CLASS_SINGLE);

	Optional<String> exception(String contextPath, String exceptionId);
	Optional<String> counters(String counterNamePrefix);

	@Override
	default PluginConfigKey<JobletExternalLinkBuilder> getKey(){
		return KEY;
	}

	@Singleton
	class JobletExternalLinkBuilderSupplier implements Supplier<JobletExternalLinkBuilder>{

		@Inject
		private PluginInjector injector;

		@Override
		public JobletExternalLinkBuilder get(){
			return injector.getInstance(KEY);
		}

	}

	class NoOpJobletExternalLinkBuilder implements JobletExternalLinkBuilder{

		@Override
		public Optional<String> exception(String contextPath, String exceptionId){
			return Optional.empty();
		}

		@Override
		public Optional<String> counters(String counterNamePrefix){
			return Optional.empty();
		}

	}

}
