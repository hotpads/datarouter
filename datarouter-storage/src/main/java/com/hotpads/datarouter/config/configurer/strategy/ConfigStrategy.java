package com.hotpads.datarouter.config.configurer.strategy;

import java.util.Optional;

public interface ConfigStrategy{

	void configure(Optional<String> optConfigDirectory);

}
