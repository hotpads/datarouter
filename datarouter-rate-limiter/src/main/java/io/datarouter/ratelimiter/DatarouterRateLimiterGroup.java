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
package io.datarouter.ratelimiter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginConfigValue;

public abstract class DatarouterRateLimiterGroup implements PluginConfigValue<DatarouterRateLimiterGroup>{

	public static final PluginConfigKey<DatarouterRateLimiterGroup> KEY = new PluginConfigKey<>(
			"rateLimiterConfigurationGroup",
			PluginConfigType.CLASS_LIST);

	private final Map<String,DatarouterRateLimiterPackage> rateLimiterPackages;

	public DatarouterRateLimiterGroup(){
		this.rateLimiterPackages = new HashMap<>();
	}

	public void registerRateLimiter(
			String name,
			Class<? extends DatarouterRateLimiter> rateLimiterClass){
		rateLimiterPackages.put(
				name,
				new DatarouterRateLimiterPackage(name, rateLimiterClass));
	}

	public List<DatarouterRateLimiterPackage> getRateLimiters(){
		return new ArrayList<>(rateLimiterPackages.values());
	}

	@Override
	public PluginConfigKey<DatarouterRateLimiterGroup> getKey(){
		return KEY;
	}

	public record DatarouterRateLimiterPackage(
			String name,
			Class<? extends DatarouterRateLimiter> rateLimiterClass){
	}

}
