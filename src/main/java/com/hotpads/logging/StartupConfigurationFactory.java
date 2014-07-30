package com.hotpads.logging;

import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory;

@Plugin(category = "ConfigurationFactory", name = "StartupConfigurationFactory")
@Order(10)
public class StartupConfigurationFactory extends XmlConfigurationFactory{

	@Override
	public String[] getSupportedTypes(){
		System.out.println("b");
		return new String[]{".xml"};
	}

	@Override
	public Configuration getConfiguration(ConfigurationSource source){
		System.out.println("a");
		return new DefaultConfiguration();
	}

}
