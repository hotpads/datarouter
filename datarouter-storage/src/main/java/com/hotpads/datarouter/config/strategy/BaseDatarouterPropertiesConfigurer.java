package com.hotpads.datarouter.config.strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseDatarouterPropertiesConfigurer{
	private static final Logger logger = LoggerFactory.getLogger(BaseDatarouterPropertiesConfigurer.class);

	private final Map<String,ConfigStrategyRunner> runnerByName;

	protected BaseDatarouterPropertiesConfigurer(){
		this.runnerByName = new HashMap<>();
	}


	protected void register(String name, ConfigStrategyRunner runner){
		runnerByName.put(name, runner);
	}

	public void configure(Optional<String> strategyName, Optional<String> optConfigDirectory){
		if(!strategyName.isPresent()){
			logger.warn("no configStrategy provided");
		}
		ConfigStrategyRunner strategy = runnerByName.get(strategyName.get());
		Objects.requireNonNull(strategy, "couldn't find registered configStrategy with name " + strategyName.get());
		strategy.configure(optConfigDirectory);
	}
}
