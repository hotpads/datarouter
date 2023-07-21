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
package io.datarouter.storage.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginInjector;
import io.datarouter.plugin.StringPluginConfigValue;
import io.datarouter.scanner.Scanner;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

// Set of email aliases to receive all datarouter emails
@Singleton
public class DatarouterSubscribersSupplier implements Supplier<Set<String>>{

	public static final PluginConfigKey<StringPluginConfigValue> KEY = new PluginConfigKey<>(
			"datarouterSubscribers",
			PluginConfigType.INSTANCE_LIST);

	@Inject
	private PluginInjector injector;

	@Override
	public Set<String> get(){
		List<StringPluginConfigValue> list = injector.getInstances(KEY);
		if(list == null){
			return Set.of();
		}
		return Scanner.of(list)
				.map(StringPluginConfigValue::getValue)
				.collect(HashSet::new);
	}

	public String getAsCsv(){
		return String.join(",", get());
	}

}
