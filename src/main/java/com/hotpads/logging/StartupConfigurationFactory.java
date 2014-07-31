package com.hotpads.logging;

import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory;

@Plugin(category = "ConfigurationFactory", name = "StartupConfigurationFactory")
@Order(5)
public class StartupConfigurationFactory extends XmlConfigurationFactory{

	@Override
	public String[] getSupportedTypes(){
		return new String[]{".hotpads"};
	}

	@Override
	public Configuration getConfiguration(ConfigurationSource source){
		System.out.println("a");
		Configuration configuration = new DefaultConfiguration();
		return new XmlConfiguration(source);
	}

}
