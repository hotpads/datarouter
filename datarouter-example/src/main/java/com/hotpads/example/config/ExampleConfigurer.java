package com.hotpads.example.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.configurer.BaseDatarouterPropertiesConfigurer;
import com.hotpads.datarouter.config.configurer.strategy.CopyResourcesFileConfigStrategy;
import com.hotpads.datarouter.config.configurer.strategy.NoneConfigStrategy;

@Singleton
public class ExampleConfigurer extends BaseDatarouterPropertiesConfigurer{

	@Inject
	public ExampleConfigurer(NoneConfigStrategy noneConfigStrategy, ServicesDevConfigStrategy devConfigStrategy){
		registerConfigStrategy("none", noneConfigStrategy);
		registerConfigStrategy("dev", devConfigStrategy);
	}

	/*----------------- strategies ----------------------*/

	@Singleton
	public static class ServicesDevConfigStrategy extends CopyResourcesFileConfigStrategy{

		private static final String TARGET_FILE = ExampleDatarouterProperties.EXAMPLE_ROUTER_CONFIG_FILE_NAME;

		public ServicesDevConfigStrategy(){
			super("/datarouter-example-dev.properties", TARGET_FILE);
		}
	}

}