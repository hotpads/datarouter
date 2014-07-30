package com.hotpads.logging;

import java.util.Map;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationListener;
import org.apache.logging.log4j.core.config.ConfigurationMonitor;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.core.net.Advertiser;

@Plugin(name = "StartupConfigurationFactory", category = "ConfigurationFactory")
@Order(10)
public class StartupConfigurationFactory extends ConfigurationFactory{

	@Override
	protected String[] getSupportedTypes(){
		System.out.println("b");
		return null;
	}

	@Override
	public Configuration getConfiguration(ConfigurationSource source){
		System.out.println("a");
		return new Configuration(){
			
			@Override
			public void stop(){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void start(){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isStopped(){
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isStarted(){
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void removeFilter(Filter filter){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isFiltered(LogEvent event){
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean hasFilter(){
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public Filter getFilter(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void addFilter(Filter filter){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setLoggerAdditive(Logger logger, boolean additive){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setConfigurationMonitor(ConfigurationMonitor monitor){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setAdvertiser(Advertiser advertiser){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void removeLogger(String name){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void removeListener(ConfigurationListener listener){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isShutdownHookEnabled(){
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public StrSubstitutor getStrSubstitutor(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Map<String,String> getProperties(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getName(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Map<String,LoggerConfig> getLoggers(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public LoggerConfig getLoggerConfig(String name){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public ConfigurationSource getConfigurationSource(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public ConfigurationMonitor getConfigurationMonitor(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public <T>T getComponent(String name){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Map<String,Appender> getAppenders(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Appender getAppender(String name){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Advertiser getAdvertiser(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void createConfiguration(Node node, LogEvent event){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void addLoggerFilter(Logger logger, Filter filter){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void addLoggerAppender(Logger logger, Appender appender){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void addLogger(String name, LoggerConfig loggerConfig){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void addListener(ConfigurationListener listener){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void addComponent(String name, Object object){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void addAppender(Appender appender){
				// TODO Auto-generated method stub
				
			}
		};
	}

}
