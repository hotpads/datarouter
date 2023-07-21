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
package io.datarouter.web.service;

import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginInjector;
import io.datarouter.plugin.StringPluginConfigValue;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ServiceDescriptionSupplier implements Supplier<String>{

	public static final PluginConfigKey<StringPluginConfigValue> KEY = new PluginConfigKey<>(
			"serviceDescription",
			PluginConfigType.INSTANCE_SINGLE);

	@Inject
	private PluginInjector injector;

	@Override
	public String get(){
		return Optional.ofNullable(injector.getInstance(KEY))
				.map(StringPluginConfigValue::getValue)
				.orElse(null);
	}

}
