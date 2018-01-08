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
package io.datarouter.storage.config.configurer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.storage.config.configurer.strategy.ConfigStrategy;

public abstract class BaseDatarouterPropertiesConfigurer{
	private static final Logger logger = LoggerFactory.getLogger(BaseDatarouterPropertiesConfigurer.class);

	private final Map<String,ConfigStrategy> configStrategyByName;

	protected BaseDatarouterPropertiesConfigurer(){
		this.configStrategyByName = new HashMap<>();
	}


	protected void registerConfigStrategy(String name, ConfigStrategy strategy){
		configStrategyByName.put(name, strategy);
	}

	public void configure(String strategyName, String configDirectory){
		if(strategyName == null){
			logger.warn("no configStrategy provided");
			return;
		}
		ConfigStrategy strategy = configStrategyByName.get(strategyName);
		Objects.requireNonNull(strategy, "couldn't find registered configStrategy with name " + strategyName);
		strategy.configure(configDirectory);
	}
}
