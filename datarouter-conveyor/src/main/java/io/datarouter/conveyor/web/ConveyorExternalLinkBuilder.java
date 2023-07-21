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
package io.datarouter.conveyor.web;

import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginConfigValue;
import io.datarouter.plugin.PluginInjector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

public interface ConveyorExternalLinkBuilder extends PluginConfigValue<ConveyorExternalLinkBuilder>{

	PluginConfigKey<ConveyorExternalLinkBuilder> KEY = new PluginConfigKey<>(
			"conveyorExternalLinkBuilder",
			PluginConfigType.CLASS_SINGLE);

	Optional<String> counters(String counterNamePrefix);
	Optional<String> exceptions(String conveyorName);
	Optional<String> traces(String conveyorName);

	@Override
	default PluginConfigKey<ConveyorExternalLinkBuilder> getKey(){
		return KEY;
	}

	@Singleton
	class ConveyorExternalLinkBuilderSupplier implements Supplier<ConveyorExternalLinkBuilder>{

		@Inject
		private PluginInjector injector;

		@Override
		public ConveyorExternalLinkBuilder get(){
			return injector.getInstance(KEY);
		}

	}

	class NoOpConveyorExternalLinkBuilder implements ConveyorExternalLinkBuilder{

		@Override
		public Optional<String> counters(String counterNamePrefix){
			return Optional.empty();
		}

		@Override
		public Optional<String> exceptions(String conveyorName){
			return Optional.empty();
		}

		@Override
		public Optional<String> traces(String conveyorName){
			return Optional.empty();
		}

	}

}
