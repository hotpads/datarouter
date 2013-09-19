package com.hotpads.job.setting;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.quartz.CronExpression;

import com.hotpads.config.ConfigRouter;
import com.hotpads.config.server.databean.ClusterSetting;
import com.hotpads.config.server.databean.ClusterSettingKey;
import com.hotpads.config.server.databean.ClusterSettingScopeComparator;
import com.hotpads.config.server.enums.ClusterSettingScope;
import com.hotpads.config.server.enums.ServerType;
import com.hotpads.job.config.instance.InstanceSettings;
import com.hotpads.util.ExternalConfig;
import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.StringTool;

@Singleton
public class ClusterSettingFinder {
	protected static Logger logger = Logger.getLogger(ClusterSettingFinder.class);
	
	public static final String PREFIX_trigger = "trigger.";
	public static final String EMPTY_STRING = "";

	protected ConfigRouter configRouter;
	protected InstanceSettings instanceSettings;
	protected ExternalConfig externalConfig;

	@Inject
	public ClusterSettingFinder(ConfigRouter configRouter, InstanceSettings instanceSettings, 
			ExternalConfig externalConfig) {
		this.configRouter = configRouter;
		this.instanceSettings = instanceSettings;
		this.externalConfig = externalConfig;
	}
	
	public Integer getInteger(String name, Integer defaultValue){
		String valueString = getMostSpecificValue(name);
		if(valueString==null){ return defaultValue; }
		return Integer.valueOf(valueString);
	}
	
	public Boolean getBoolean(String name, Boolean defaultValue) {
		String valueString = getMostSpecificValue(name);
		if(valueString==null){ return defaultValue; }
		return BooleanTool.isTrue(valueString);
	}
	
	public String getString(String name, String defaultValue){
		String valueString = getMostSpecificValue(name);
		if(valueString==null){ return defaultValue; }
		return valueString;
	}
	
	public CronExpression getCronExpression(String name, String defaultValue){
		String valueString = getMostSpecificValue(name);
		try {
			if(valueString==null){ 
				valueString = defaultValue;
			}
			if(valueString==null){
				return null;
			}
			return new CronExpression(valueString);
		} catch (ParseException e) {
			logger.warn("ParseException on "+name+" with value "+valueString);
			throw new RuntimeException(e);
		}
	}
	
	protected String getMostSpecificValue(String name){
		List<ClusterSettingKey> keys = generateKeysForSelection(name);
//		boolean log = "job.event.aggregateEvents".equals(name);
//		if(log){
//			logger.warn("searching for "+CollectionTool.size(keys)+":");
//			for(ClusterSettingKey key : keys){ logger.warn(key); }
//		}
		List<ClusterSetting> settings = configRouter.clusterSetting.getMulti(keys, null);
//		if(log){
//			logger.warn("found "+CollectionTool.size(settings)+":");
//			for(ClusterSetting setting : settings){ System.out.println(setting); }
//		}
		if(CollectionTool.isEmpty(settings)){ return null; }
		Collections.sort(settings, new ClusterSettingScopeComparator());
		return CollectionTool.getFirst(settings).getValue();
	}
	
	//TODO should we be making combinations like serverType/instance?
	protected List<ClusterSettingKey> generateKeysForSelection(String name){
		List<ClusterSettingKey> keys = ListTool.createArrayList();
		
		//remember to use "" instead of null.  should probably make a new Field type to do that for you
		keys.add(new ClusterSettingKey(name, ClusterSettingScope.defaultScope, ServerType.UNKNOWN, EMPTY_STRING,
				EMPTY_STRING));

		keys.add(new ClusterSettingKey(name, ClusterSettingScope.cluster, ServerType.ALL, EMPTY_STRING, EMPTY_STRING));
		
		ClusterSettingKey serverTypeSetting = getKeyForServerType(name);
		if(serverTypeSetting != null){ keys.add(serverTypeSetting); }
		
		ClusterSettingKey instanceSetting = getKeyForInstance(name);
		if(instanceSetting != null){ keys.add(instanceSetting); }
		
		ClusterSettingKey applicationSetting = getKeyForApplication(name);
		if(applicationSetting != null){ keys.add(applicationSetting); }
		
		return keys;
	}
	
	protected ClusterSettingKey getKeyForServerType(String name){
		ServerType serverType = externalConfig.getServerType();
		if(serverType==null || serverType==ServerType.UNKNOWN){ return null; }
		return new ClusterSettingKey(name, ClusterSettingScope.serverType, serverType, EMPTY_STRING,
				EMPTY_STRING);
	}
	
	protected ClusterSettingKey getKeyForInstance(String name){
		//TODO: use InstanceSettings for instance
		String instance = externalConfig.getInstanceId();
		if(StringTool.isEmpty(instance)){ return null; }
		return new ClusterSettingKey(name, ClusterSettingScope.instance, ServerType.UNKNOWN, instance, EMPTY_STRING);
	}
	
	protected ClusterSettingKey getKeyForApplication(String name){
		String application = instanceSettings.getApplication();
		if(StringTool.isEmpty(application)){ return null; }
		return new ClusterSettingKey(name, ClusterSettingScope.application, ServerType.UNKNOWN, application, 
				EMPTY_STRING);
	}
	
	public List<CronExpression> getAllTriggers(){
		List<ClusterSetting> settings = configRouter.clusterSetting.getWithPrefix(new ClusterSettingKey(
				PREFIX_trigger, null, null, null, null), true, null);
		List<CronExpression> triggers = ListTool.createArrayList();
		for(ClusterSetting setting : IterableTool.nullSafe(settings)){
			String triggerString = setting.getValue();
			try{
				CronExpression trigger = new CronExpression(triggerString);
				triggers.add(trigger);
			}catch(ParseException pe){
				logger.warn("ParseException on "+setting+" with value "+triggerString);
			}
		}
		return triggers;
	}
	
	
}
