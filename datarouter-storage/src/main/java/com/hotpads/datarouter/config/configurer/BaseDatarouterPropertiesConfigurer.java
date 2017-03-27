package com.hotpads.datarouter.config.configurer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.config.configurer.strategy.ConfigStrategy;

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
