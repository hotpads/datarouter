package com.hotpads.datarouter.config.strategy;

import java.util.Optional;

public interface ConfigStrategyRunner{

	void configure(Optional<String> optConfigDirectory);

}
