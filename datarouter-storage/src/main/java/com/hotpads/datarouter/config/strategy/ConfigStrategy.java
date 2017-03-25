package com.hotpads.datarouter.config.strategy;

import java.util.Optional;

public interface ConfigStrategy{

	void configure(Optional<String> optConfigDirectory);

}
